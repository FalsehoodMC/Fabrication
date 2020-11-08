package com.unascribed.fabrication;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import org.apache.commons.lang3.reflect.FieldUtils;
import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;
import com.unascribed.fabrication.support.ResolvedTrilean;
import com.unascribed.fabrication.support.Trilean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class FabricationMod implements ModInitializer {
	
	public static final Direction.Type NULL_DIRECTION_TYPE;
	static {
		try {
			Class<Direction.Type> clazz = Direction.Type.class;
			Constructor<Direction.Type> cons = clazz.getDeclaredConstructor(String.class, int.class, Direction[].class, Direction.Axis[].class);
			cons.setAccessible(true);
			// have to do this to bypass the enum check
			sun.reflect.ReflectionFactory rf = sun.reflect.ReflectionFactory.getReflectionFactory();
			NULL_DIRECTION_TYPE = (Direction.Type)rf.newConstructorAccessor(cons)
					.newInstance(new Object[] {"FABRICATION$NULL", -1, new Direction[0], new Direction.Axis[0]});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final Map<String, Feature> features = Maps.newHashMap();
	private static final List<Feature> unconfigurableFeatures = Lists.newArrayList();
	private static final Set<String> enabledFeatures = Sets.newHashSet();
	
	public static final long LAUNCH_ID = ThreadLocalRandom.current().nextLong();
	
	public static SoundEvent LEVELUP_LONG;
	
	@Override
	public void onInitialize() {
		for (String str : MixinConfigPlugin.discoverClassesInPackage("com.unascribed.fabrication.loaders", false)) {
			try {
				MixinConfigPlugin.introduce((ConfigLoader)Class.forName(str).newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		for (String s : MixinConfigPlugin.discoverClassesInPackage("com.unascribed.fabrication.features", false)) {
			try {
				Feature r = (Feature)Class.forName(s).newInstance();
				String key = MixinConfigPlugin.remap(r.getConfigKey());
				if (key == null || RuntimeChecks.check(key)) {
					r.apply();
					if (key != null) {
						enabledFeatures.add(key);
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
		if (MixinConfigPlugin.getValue("*.long_levelup_sound_at_30") != Trilean.FALSE && FabricLoader.getInstance().isModLoaded("fabric")) {
			LEVELUP_LONG = Registry.register(Registry.SOUND_EVENT, new Identifier("fabrication", "levelup_long"), new SoundEvent(new Identifier("fabrication", "levelup_long")));
		}
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
	
	public static <T> T snag(Class<?> clazz, Object inst, String intermediateName, String yarnName) {
		try {
			return (T)snagField(clazz, intermediateName, yarnName).get(inst);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Field snagField(Class<?> clazz, String intermediateName, String yarnName) {
		try {
			Field f;
			try {
				f = clazz.getDeclaredField(intermediateName);
			} catch (NoSuchFieldException e) {
				f = clazz.getDeclaredField(yarnName);
			}
			f.setAccessible(true);
			return f;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> void shove(Class<?> clazz, Object inst, String intermediateName, String yarnName, Object value) {
		try {
			Field f = snagField(clazz, intermediateName, yarnName);
			f.setAccessible(true);
			FieldUtils.removeFinalModifier(f);
			f.set(inst, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T twiddle(Class<?> clazz, Object inst, String intermediateName, String yarnName, Class<?>[] argTypes, Object... args) {
		try {
			Method m;
			try {
				m = clazz.getDeclaredMethod(intermediateName, argTypes);
			} catch (NoSuchMethodException e) {
				m = clazz.getDeclaredMethod(yarnName, argTypes);
			}
			m.setAccessible(true);
			return (T)m.invoke(inst, args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static void sendToTrackersMatching(Entity entity, CustomPayloadS2CPacket pkt, Predicate<ServerPlayerEntity> predicate) {
		if (entity.world.isClient) return;
		ServerChunkManager cm = ((ServerWorld)entity.world).getChunkManager();
		ThreadedAnvilChunkStorage tacs = cm.threadedAnvilChunkStorage;
		Int2ObjectMap<?> entityTrackers = FabricationMod.snag(ThreadedAnvilChunkStorage.class, tacs, "field_18242", "entityTrackers");
		Object tracker = entityTrackers.get(entity.getEntityId());
		if (tracker == null) return;
		Set<ServerPlayerEntity> playersTracking = FabricationMod.snag(tracker.getClass(), tracker, "field_18250", "playersTracking");
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
			Map<String, ResolvedTrilean> trileans = Maps.newHashMap();
			Map<String, String> strings = Maps.newHashMap();
			for (String k : MixinConfigPlugin.getAllKeys()) {
				if (MixinConfigPlugin.isTrilean(k)) {
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
			if (MixinConfigPlugin.isTrilean(key)) {
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
