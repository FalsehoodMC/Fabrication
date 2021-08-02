package com.unascribed.fabrication;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.ConfigValue;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.OptionalFScript;
import com.unascribed.fabrication.support.ResolvedConfigValue;
import com.unascribed.fabrication.support.SpecialEligibility;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.github.queerbric.pride.PrideClient;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FabricationMod implements ModInitializer {

	public static final String MOD_NAME = MixinConfigPlugin.isMet(SpecialEligibility.FORGE) ? "Forgery" : "Fabrication";
	// NOT the modid. We keep the mod id as "fabrication" even on Forge to keep things from getting too nutty.
	public static final String MOD_NAME_LOWER = MixinConfigPlugin.isMet(SpecialEligibility.FORGE) ? "forgery" : "fabrication";
	
	private static final Map<String, Feature> features = Maps.newHashMap();
	private static final List<Feature> unconfigurableFeatures = Lists.newArrayList();
	private static final Set<String> enabledFeatures = Sets.newHashSet();

	public static final long LAUNCH_ID = ThreadLocalRandom.current().nextLong();

	public static SoundEvent LEVELUP_LONG;
	public static SoundEvent OOF;
	public static SoundEvent ABSORPTION_HURT;

	@Override
	public void onInitialize() {
		MixinConfigPlugin.loadComplete = true;
		for (String str : MixinConfigPlugin.discoverClassesInPackage("com.unascribed.fabrication.loaders", false)) {
			try {
				MixinConfigPlugin.introduce((ConfigLoader)Class.forName(str).newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (Agnos.isModLoaded("fscript")) OptionalFScript.reload();
		for (String s : MixinConfigPlugin.discoverClassesInPackage("com.unascribed.fabrication.features", false)) {
			try {
				Feature r = (Feature)Class.forName(s).newInstance();
				String key = MixinConfigPlugin.remap(r.getConfigKey());
				if (key == null || MixinConfigPlugin.isEnabled(key)) {
					try {
						r.apply();
						if (key != null) {
							enabledFeatures.add(key);
						}
					} catch (Throwable t) {
						featureError(r, t);
						continue;
					}
				}
				if (key != null) {
					features.put(key, r);
				} else {
					unconfigurableFeatures.add(r);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to initialize feature "+s, e);
			}
		}
		if (Agnos.eventsAvailable() && Agnos.getCurrentEnv() == Env.CLIENT) {
			if (MixinConfigPlugin.getValue("*.long_levelup_sound_at_30") != ConfigValue.FALSE) {
				LEVELUP_LONG = Agnos.registerSoundEvent(new Identifier("fabrication", "levelup_long"), new SoundEvent(new Identifier("fabrication", "levelup_long")));
			}
			if (MixinConfigPlugin.getValue("*.oof") != ConfigValue.FALSE) {
				OOF = Agnos.registerSoundEvent(new Identifier("fabrication", "oof"), new SoundEvent(new Identifier("fabrication", "oof")));
			}
			if (MixinConfigPlugin.getValue("*.alt_absorption_sound") != ConfigValue.FALSE) {
				ABSORPTION_HURT = Agnos.registerSoundEvent(new Identifier("fabrication", "absorption_hurt"), new SoundEvent(new Identifier("fabrication", "absorption_hurt")));
			}
		}
		MixinConfigPlugin.submitConfigAnalytics();
		Analytics.submit("game_launch");
		if (FabRefl.FORGE && Agnos.getCurrentEnv() == Env.CLIENT) {
			initPrideLib();
		}
	}

	@Environment(EnvType.CLIENT)
	private void initPrideLib() {
		new PrideClient().onInitializeClient();
	}

	public static void featureError(Feature f, Throwable t) {
		featureError(f.getClass(), f.getConfigKey(), t);
	}

	public static void featureError(Class<?> clazz, String configKey, Throwable t) {
		FabLog.debug("Original feature error", t);
		if (configKey == null) {
			FabLog.warn("Feature "+clazz.getName()+" failed to apply!");
		} else {
			FabLog.warn("Feature "+clazz.getName()+" failed to apply! Force-disabling "+configKey);
		}
		MixinConfigPlugin.addFailure(configKey);
	}

	public static Identifier createIdWithCustomDefault(String namespace, String pathOrId) {
		if (pathOrId.contains(":")) {
			return new Identifier(pathOrId);
		}
		return new Identifier(namespace, pathOrId);
	}

	public static boolean isAvailableFeature(String configKey) {
		return features.containsKey(MixinConfigPlugin.remap(configKey));
	}

	public static boolean updateFeature(String configKey) {
		configKey = MixinConfigPlugin.remap(configKey);
		boolean enabled = MixinConfigPlugin.isEnabled(configKey);
		if (enabledFeatures.contains(configKey) == enabled) return true;
		if (enabled) {
			features.get(configKey).apply();
			enabledFeatures.add(configKey);
			return true;
		} else {
			boolean b = features.get(configKey).undo();
			if (b) {
				enabledFeatures.remove(configKey);
			}
			return b;
		}
	}

	public static Set<ServerPlayerEntity> getTrackers(Entity entity) {
		ServerChunkManager cm = ((ServerWorld)entity.world).getChunkManager();
		ThreadedAnvilChunkStorage tacs = cm.threadedAnvilChunkStorage;
		Int2ObjectMap<EntityTracker> entityTrackers = FabRefl.getEntityTrackers(tacs);
		EntityTracker tracker = entityTrackers.get(entity.getEntityId());
		if (tracker == null) return Collections.emptySet();
		return FabRefl.getPlayersTracking(tracker);
	}

	public static void sendToTrackersMatching(Entity entity, CustomPayloadS2CPacket pkt, Predicate<ServerPlayerEntity> predicate) {
		if (entity.world.isClient) return;
		Set<ServerPlayerEntity> playersTracking = getTrackers(entity);
		if (entity instanceof ServerPlayerEntity) {
			ServerPlayerEntity spe = (ServerPlayerEntity)entity;
			if (predicate.test(spe)) {
				spe.networkHandler.sendPacket(pkt);
			}
		}
		for (ServerPlayerEntity spe : playersTracking) {
			if (predicate.test(spe)) {
				spe.networkHandler.sendPacket(pkt);
			}
		}
	}

	public static void sendConfigUpdate(MinecraftServer server, String key) {
		for (ServerPlayerEntity spe : server.getPlayerManager().getPlayerList()) {
			if (spe instanceof SetFabricationConfigAware && ((SetFabricationConfigAware)spe).fabrication$isConfigAware()) {
				sendConfigUpdate(server, key, spe);
			}
		}
	}

	private static final Identifier CONFIG = new Identifier("fabrication", "config");

	public static void sendConfigUpdate(MinecraftServer server, String key, ServerPlayerEntity spe) {
		if ("general.profile".equals(key)) key = null;
		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		if (key == null) {
			Map<String, ResolvedConfigValue> trileans = Maps.newHashMap();
			Map<String, String> strings = Maps.newHashMap();
			for (String k : MixinConfigPlugin.getAllKeys()) {
				if (MixinConfigPlugin.isStandardValue(k)) {
					trileans.put(k, MixinConfigPlugin.getResolvedValue(k));
				} else {
					strings.put(k, MixinConfigPlugin.getRawValue(k));
				}
			}
			data.writeVarInt(trileans.size());
			trileans.entrySet().forEach(en -> data.writeString(en.getKey()).writeByte(en.getValue().ordinal()));
			data.writeVarInt(strings.size());
			strings.entrySet().forEach(en -> data.writeString(en.getKey()).writeString(en.getValue()));
			data.writeLong(LAUNCH_ID);
		} else {
			if (MixinConfigPlugin.isStandardValue(key)) {
				data.writeVarInt(1);
				data.writeString(key);
				data.writeByte(MixinConfigPlugin.getResolvedValue(key).ordinal());
				data.writeVarInt(0);
				data.writeLong(LAUNCH_ID);
			} else {
				data.writeVarInt(0);
				data.writeVarInt(1);
				data.writeString(key);
				data.writeString(MixinConfigPlugin.getRawValue(key));
				data.writeLong(LAUNCH_ID);
			}
		}
		data.writeString(Agnos.getModVersion());
		data.writeVarInt(MixinConfigPlugin.getAllFailures().size());
		for (String k : MixinConfigPlugin.getAllFailures()) {
			data.writeString(k);
		}
		data.writeVarInt(MixinConfigPlugin.getAllBanned().size());
		for (String k : MixinConfigPlugin.getAllBanned()) {
			data.writeString(k);
		}
		CustomPayloadS2CPacket pkt = new CustomPayloadS2CPacket(CONFIG, data);
		spe.networkHandler.sendPacket(pkt);
	}

	private static final BlockPos.Mutable scratchpos1 = new BlockPos.Mutable();
	private static final BlockPos.Mutable scratchpos2 = new BlockPos.Mutable();
	private static final BlockPos.Mutable scratchpos3 = new BlockPos.Mutable();
	private static final BlockPos.Mutable scratchpos4 = new BlockPos.Mutable();

	public interface BlockScanCallback {
		boolean invoke(World w, BlockPos.Mutable bp, BlockPos.Mutable scratch, Direction dir);
	}

	public static void forAllAdjacentBlocks(Entity entity, BlockScanCallback callback) {
		World w = entity.world;
		Box box = entity.getBoundingBox();
		if (!scanBlocks(w, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, Direction.DOWN, callback)) return;
		if (!scanBlocks(w, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, Direction.UP, callback)) return;

		if (!scanBlocks(w, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ, Direction.WEST, callback)) return;
		if (!scanBlocks(w, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, Direction.EAST, callback)) return;

		if (!scanBlocks(w, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, Direction.NORTH, callback)) return;
		if (!scanBlocks(w, box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, Direction.SOUTH, callback)) return;
	}

	private static boolean scanBlocks(World w, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Direction dir,
			BlockScanCallback callback) {
		BlockPos min = scratchpos1.set(minX+dir.getOffsetX(), minY+dir.getOffsetY(), minZ+dir.getOffsetZ());
		BlockPos max = scratchpos2.set(maxX+dir.getOffsetX(), maxY+dir.getOffsetY(), maxZ+dir.getOffsetZ());
		BlockPos.Mutable mut = scratchpos3;
		if (w.isRegionLoaded(min, max)) {
			for (int x = min.getX(); x <= max.getX(); x++) {
				for (int y = min.getY(); y <= max.getY(); y++) {
					for (int z = min.getZ(); z <= max.getZ(); z++) {
						mut.set(x, y, z);
						scratchpos4.set(mut);
						if (!callback.invoke(w, mut, scratchpos4, dir)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

}
