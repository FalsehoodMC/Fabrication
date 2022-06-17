package com.unascribed.fabrication;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.unascribed.fabrication.support.FabReflField;
import com.unascribed.fabrication.support.injection.FabRefMap;
import org.spongepowered.asm.mixin.throwables.MixinError;
import org.spongepowered.asm.mixin.throwables.MixinException;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unascribed.fabrication.support.MixinErrorHandler;

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.FireBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImage.Format;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.server.command.GiveCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker;
import net.minecraft.util.Identifier;

public class FabRefl {

	// "muh performance"
	// invokeExact becomes a standard INVOKE* insn after the JIT gets its hands on it. The entire
	// purpose of MethodHandles is to be basically free. the catch is there can be *no* abstraction
	// on an invokeExact or the calltime signature will be wrong and the JVM will get confused.

	@FabReflField
	private static final String cpc2sp_channel_field = "net/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;channel";
	private static final MethodHandle cpc2sp_channel = unreflectGetter("CustomPayloadC2SPacket", () -> CustomPayloadC2SPacket.class, cpc2sp_channel_field).get();
	public static Identifier getChannel(CustomPayloadC2SPacket subject) {
		try {
			return (Identifier)checkHandle(cpc2sp_channel).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String cpc2sp_data_field = "net/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;data";
	private static final MethodHandle cpc2sp_data = unreflectGetter("CustomPayloadC2SPacket", () -> CustomPayloadC2SPacket.class, cpc2sp_data_field).get();
	public static PacketByteBuf getData(CustomPayloadC2SPacket subject) {
		try {
			return (PacketByteBuf)checkHandle(cpc2sp_data).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String feg_withinRangePredicate_field = "net/minecraft/entity/ai/goal/FleeEntityGoal;withinRangePredicate";
	private static final MethodHandle feg_withinRangePredicate = unreflectGetter("FleeEntityGoal", () -> FleeEntityGoal.class, feg_withinRangePredicate_field)
			.requiredBy("*.taggable_players").get();
	public static TargetPredicate getWithinRangePredicate(FleeEntityGoal<?> subject) {
		try {
			return (TargetPredicate)checkHandle(feg_withinRangePredicate).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String es_basePredicate_field = "net/minecraft/command/EntitySelector;basePredicate";
	private static final MethodHandle es_basePredicate = unreflectGetter("EntitySelector", () -> EntitySelector.class, es_basePredicate_field)
			.requiredBy("*.canhit").get();
	public static Predicate<Entity> getBasePredicate(EntitySelector subject) {
		try {
			return (Predicate<Entity>)checkHandle(es_basePredicate).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String tacs_entityTrackers_field = "net/minecraft/server/world/ThreadedAnvilChunkStorage;entityTrackers";
	private static final MethodHandle tacs_entityTrackers = unreflectGetter("ThreadedAnvilChunkStorage", () -> ThreadedAnvilChunkStorage.class, tacs_entityTrackers_field)
			.requiredBy("*.sync_attacker_yaw", "*.despawning_items_blink").get();
	public static Int2ObjectMap<EntityTracker> getEntityTrackers(ThreadedAnvilChunkStorage subject) {
		try {
			return (Int2ObjectMap<EntityTracker>)checkHandle(tacs_entityTrackers).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String et_playersTracking_field = "net/minecraft/server/world/ThreadedAnvilChunkStorage$EntityTracker;listeners";
	private static final MethodHandle et_playersTracking = unreflectGetter("EntityTracker", () -> EntityTracker.class, et_playersTracking_field)
			.requiredBy("*.sync_attacker_yaw", "*.despawning_items_blink").get();
	public static Set<EntityTrackingListener> getPlayersTracking(EntityTracker subject) {
		try {
			return (Set<EntityTrackingListener>)checkHandle(et_playersTracking).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String rpm_providers_get_field = "net/minecraft/resource/ResourcePackManager;providers";
	private static final MethodHandle rpm_providers_get = unreflectGetter("ResourcePackManager", () -> ResourcePackManager.class, rpm_providers_get_field)
			.requiredBy("*.oak_is_apple", "*.tnt_is_dynamite").get();
	public static Set<ResourcePackProvider> getProviders(ResourcePackManager subject) {
		try {
			return (Set<ResourcePackProvider>)checkHandle(rpm_providers_get).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	private static final MethodHandle rpm_providers_set = unreflectSetter("ResourcePackManager", () -> ResourcePackManager.class, rpm_providers_get_field)
			.requiredBy("*.oak_is_apple", "*.tnt_is_dynamite").get();
	public static void setProviders(ResourcePackManager subject, Set<ResourcePackProvider> providers) {
		try {
			checkHandle(rpm_providers_set).invokeExact(subject, providers);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String abs_hardness_get_field = "net/minecraft/block/AbstractBlock$AbstractBlockState;hardness";
	private static final MethodHandle abs_hardness_get = unreflectGetter("AbstractBlockState", () -> AbstractBlockState.class, abs_hardness_get_field)
			.requiredBy("*.faster_obsidian").get();
	public static float getHardness(AbstractBlockState subject) {
		try {
			return (float)checkHandle(abs_hardness_get).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	private static final MethodHandle abs_hardness_set = unreflectSetter("AbstractBlockState", () -> AbstractBlockState.class, abs_hardness_get_field)
			.requiredBy("*.faster_obsidian").get();
	public static void setHardness(AbstractBlockState subject, float hardness) {
		try {
			checkHandle(abs_hardness_set).invokeExact(subject, hardness);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String fb_burnChances_field = "net/minecraft/block/FireBlock;burnChances";
	private static final MethodHandle fb_burnChances = unreflectGetter("FireBlock", () -> FireBlock.class, fb_burnChances_field)
			.requiredBy("*.flammable_cobwebs").get();
	public static Object2IntMap<Block> getBurnChances(FireBlock subject) {
		try {
			return (Object2IntMap<Block>)checkHandle(fb_burnChances).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String fb_spreadChances_field = "net/minecraft/block/FireBlock;spreadChances";
	private static final MethodHandle fb_spreadChances = unreflectGetter("FireBlock", () -> FireBlock.class, fb_spreadChances_field)
			.requiredBy("*.flammable_cobwebs").get();
	public static Object2IntMap<Block> getSpreadChances(FireBlock subject) {
		try {
			return (Object2IntMap<Block>)checkHandle(fb_spreadChances).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String ie_pickupDelay_field = "net/minecraft/entity/ItemEntity;pickupDelay";
	private static final MethodHandle ie_pickupDelay = unreflectGetter("ItemEntity", () -> ItemEntity.class, ie_pickupDelay_field)
			.requiredBy("*.instant_pickup").get();
	public static int getPickupDelay(ItemEntity subject) {
		try {
			return (int)checkHandle(ie_pickupDelay).invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String gc_execute_field = "Lnet/minecraft/server/command/GiveCommand;execute(Lnet/minecraft/server/command/ServerCommandSource;Lnet/minecraft/command/argument/ItemStackArgument;Ljava/util/Collection;I)I";
	private static final MethodHandle gc_execute = unreflectMethod("GiveCommand", () -> GiveCommand.class, gc_execute_field,
			int.class,
			ServerCommandSource.class, ItemStackArgument.class, Collection.class, int.class)
			.requiredBy("*.i_and_more").get();
	public static int GiveCommand_execute(ServerCommandSource source, ItemStackArgument item, Collection<ServerPlayerEntity> targets, int count) throws CommandSyntaxException {
		try {
			return (int)checkHandle(gc_execute).invokeExact(source, item, targets, count);
		} catch (Throwable t) {
			throw rethrow(t, CommandException.class);
		}
	}

	@FabReflField
	private static final String me_getDropChance_field = "Lnet/minecraft/entity/mob/MobEntity;getDropChance(Lnet/minecraft/entity/EquipmentSlot;)F";
	private static final MethodHandle me_getDropChance = unreflectMethod("MobEntity", () -> MobEntity.class, me_getDropChance_field,
			float.class,
			EquipmentSlot.class)
			.requiredBy("*.broken_tools_drop_components").get();
	public static float MobEntity_getDropChance(MobEntity subject, EquipmentSlot slot) {
		try {
			return (float)checkHandle(me_getDropChance).invokeExact(subject, slot);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@FabReflField
	private static final String fb_registerFlammableBlock_field = "Lnet/minecraft/block/FireBlock;registerFlammableBlock(Lnet/minecraft/block/Block;II)V";
	private static final MethodHandle fb_registerFlammableBlock = unreflectMethod("FireBlock", () -> FireBlock.class, fb_registerFlammableBlock_field,
			void.class,
			Block.class, int.class, int.class)
			.requiredBy("*.flammable_cobwebs").get();
	public static void FireBlock_registerFlammableBlock(FireBlock subject, Block block, int burnChance, int spreadChance) {
		try {
			checkHandle(fb_registerFlammableBlock).invokeExact(subject, block, burnChance, spreadChance);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@Environment(EnvType.CLIENT)
	public static final class Client {

		@FabReflField
		private static final String satd_width_field = "net/minecraft/client/texture/SpriteAtlasTexture$Data;width";
		private static final MethodHandle satd_width = unreflectGetter("SpriteAtlasTexture.Data", () -> SpriteAtlasTexture.Data.class, satd_width_field)
				.requiredBy("*.old_lava").get();
		public static int getWidth(SpriteAtlasTexture.Data subject) {
			try {
				return (int)checkHandle(satd_width).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String satd_height_field = "net/minecraft/client/texture/SpriteAtlasTexture$Data;height";
		private static final MethodHandle satd_height = unreflectGetter("SpriteAtlasTexture.Data", () -> SpriteAtlasTexture.Data.class, satd_height_field)
				.requiredBy("*.old_lava").get();
		public static int getHeight(SpriteAtlasTexture.Data subject) {
			try {
				return (int)checkHandle(satd_height).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String satd_maxLevel_field = "net/minecraft/client/texture/SpriteAtlasTexture$Data;maxLevel";
		private static final MethodHandle satd_maxLevel = unreflectGetter("SpriteAtlasTexture.Data", () -> SpriteAtlasTexture.Data.class, satd_maxLevel_field)
				.requiredBy("*.old_lava").get();
		public static int getMaxLevel(SpriteAtlasTexture.Data subject) {
			try {
				return (int)checkHandle(satd_maxLevel).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String sprite_x_field = "net/minecraft/client/texture/Sprite;x";
		private static final MethodHandle sprite_x = unreflectGetter("Sprite", () -> Sprite.class, sprite_x_field)
				.requiredBy("*.old_lava", "atlas_viewer").get();
		public static int getX(Sprite subject) {
			try {
				return (int)checkHandle(sprite_x).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String sprite_y_field = "net/minecraft/client/texture/Sprite;y";
		private static final MethodHandle sprite_y = unreflectGetter("Sprite", () -> Sprite.class, sprite_y_field)
				.requiredBy("*.old_lava", "atlas_viewer").get();
		public static int getY(Sprite subject) {
			try {
				return (int)checkHandle(sprite_y).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String spriteAnimation_frameIndex_field = "net/minecraft/client/texture/Sprite$Animation;frameIndex";
		private static final MethodHandle spriteAnimation_frameIndex = unreflectGetter("Sprite.Animation", () -> Sprite.Animation.class, spriteAnimation_frameIndex_field)
				.requiredBy("atlas_viewer").get();
		public static int getFrameIndex(Sprite.Animation subject) {
			try {
				return (int)checkHandle(spriteAnimation_frameIndex).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String spriteAnimation_frameTicks_field = "net/minecraft/client/texture/Sprite$Animation;frameTicks";
		private static final MethodHandle spriteAnimation_frameTicks = unreflectGetter("Sprite.Animation", () -> Sprite.Animation.class, spriteAnimation_frameTicks_field)
				.requiredBy("atlas_viewer").get();
		public static int getFrameTicks(Sprite.Animation subject) {
			try {
				return (int)checkHandle(spriteAnimation_frameTicks).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String sat_sprites_field = "net/minecraft/client/texture/SpriteAtlasTexture;sprites";
		private static final MethodHandle sat_sprites = unreflectGetter("SpriteAtlasTexture", () -> SpriteAtlasTexture.class, sat_sprites_field)
				.requiredBy("*.old_lava", "atlas_viewer").get();
		public static Map<Identifier, Sprite> getSprites(SpriteAtlasTexture subject) {
			try {
				return (Map<Identifier, Sprite>)checkHandle(sat_sprites).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String sat_animatedSprites_field = "net/minecraft/client/texture/SpriteAtlasTexture;animatedSprites";
		private static final MethodHandle sat_animatedSprites = unreflectGetter("SpriteAtlasTexture", () -> SpriteAtlasTexture.class, sat_animatedSprites_field)
				.requiredBy("*.old_lava").get();
		public static List<Sprite> getAnimatedSprites(SpriteAtlasTexture subject) {
			try {
				return (List<Sprite>)checkHandle(sat_animatedSprites).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String mc_itemColors_field = "net/minecraft/client/MinecraftClient;itemColors";
		private static final MethodHandle mc_itemColors = unreflectGetter("MinecraftClient", () -> MinecraftClient.class, mc_itemColors_field)
				.requiredBy("*.colored_crack_particles", "*.classic_block_drops").get();
		public static ItemColors getItemColors(MinecraftClient subject) {
			try {
				return (ItemColors)checkHandle(mc_itemColors).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String mh_blend_field = "Lnet/minecraft/client/texture/MipmapHelper;blend(IIIIZ)I";
		private static final MethodHandle mh_blend = unreflectMethod("MipmapHelper", () -> MipmapHelper.class, mh_blend_field,
				int.class,
				int.class, int.class, int.class, int.class, boolean.class)
				.requiredBy("*.old_lava").get();
		public static int MipmapHelper_blend(int one, int two, int three, int four, boolean checkAlpha) {
			try {
				return (int)checkHandle(mh_blend).invokeExact(one, two, three, four, checkAlpha);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		private static final MethodHandle ni_new = unreflectConstructor("NativeImage", () -> NativeImage.class,
				Format.class, int.class, int.class, boolean.class, long.class)
				.requiredBy("*.classic_block_drops").get();
		public static NativeImage NativeImage_new(Format format, int width, int height, boolean useStb, long pointer) {
			try {
				return (NativeImage)checkHandle(ni_new).invokeExact(format, width, height, useStb, pointer);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String ir_renderBakedItemModel_field = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V";
		private static final MethodHandle ir_renderBakedItemModel = unreflectMethod("ItemRenderer", () -> ItemRenderer.class, ir_renderBakedItemModel_field,
				void.class,
				BakedModel.class, ItemStack.class, int.class, int.class, MatrixStack.class, VertexConsumer.class)
				.requiredBy("*.classic_block_drops").get();
		public static void ItemRenderer_renderBakedItemModel(ItemRenderer subject, BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
			try {
				checkHandle(ir_renderBakedItemModel).invokeExact(subject, model, stack, light, overlay, matrices, vertices);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		@FabReflField
		private static final String s_getFrameCount_field = "Lnet/minecraft/client/texture/Sprite;getFrameCount()I";
		private static final MethodHandle s_getFrameCount = unreflectMethod("Sprite", () -> Sprite.class, s_getFrameCount_field,
				int.class)
				.requiredBy("*.atlas_viewer").get();
		public static int getFrameCount(Sprite subject) {
			try {
				return (int)checkHandle(s_getFrameCount).invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

	}

	private static MethodHandle checkHandle(MethodHandle handle) {
		if (handle == null) throw new IllegalStateException("Attempt to use an unresolved method handle");
		return handle;
	}

	private static final class UnreflResult {
		private final String desc;
		private final MethodHandle val;
		private final Throwable err;

		private final Set<String> requiredBy = Sets.newHashSet();

		private UnreflResult(String desc, MethodHandle val, Throwable err) {
			this.desc = desc;
			this.val = val;
			this.err = err;
		}

		public UnreflResult requiredBy(String... features) {
			for (String f : features) requiredBy.add(f);
			return this;
		}

		public MethodHandle get() {
			if (err != null) {
				if (requiredBy.isEmpty()) throw rethrow(err);
				FabLog.warn("Failed to retrieve "+desc+" - force-disabling "+Joiner.on(", ").join(requiredBy));
				for (String s : requiredBy) {
					FabConf.addFailure(s);
				}
				return null;
			}
			return val;
		}

		public static UnreflResult success(String desc, MethodHandle handle) {
			return new UnreflResult(desc, handle, null);
		}
		public static UnreflResult failure(String desc, Throwable err) {
			return new UnreflResult(desc, null, err);
		}
	}

	private static UnreflResult unreflectGetter(String className, Supplier<Class<?>> clazz, String yarnName) {
		String name = FabRefMap.methodMap("FabRefl", yarnName);
		int col = name.indexOf(';');
		if (col != -1) name = name.substring(col+1);
		String desc = "field "+className+"#"+name+" (deobf name "+yarnName+")";
		try {
			Field f = clazz.get().getDeclaredField(name);
			f.setAccessible(true);
			return UnreflResult.success(desc, MethodHandles.lookup().unreflectGetter(f));
		} catch (Throwable t) {
			return UnreflResult.failure(desc, t);
		}
	}

	private static UnreflResult unreflectSetter(String className, Supplier<Class<?>> clazz, String yarnName) {
		String name = FabRefMap.methodMap("FabRefl", yarnName);
		int col = name.indexOf(';');
		if (col != -1) name = name.substring(col+1);
		String desc = "field "+className+"#"+name+" (deobf name "+yarnName+")";
		try {
			Field f = clazz.get().getDeclaredField(name);
			f.setAccessible(true);
			return UnreflResult.success(desc, MethodHandles.lookup().unreflectSetter(f));
		} catch (Throwable t) {
			return UnreflResult.failure(desc, t);
		}
	}

	private static UnreflResult unreflectMethod(String className, Supplier<Class<?>> clazz, String yarnName, Class<?> returnType, Class<?>... args) {
		String name = FabRefMap.targetMap("FabRefl", yarnName);
		name = name.substring(name.indexOf(';')+1, name.indexOf('('));
		String desc = "method "+className+"."+name+signatureToString(args)+" (deobf name "+yarnName+")";
		try {
			Method m = clazz.get().getDeclaredMethod(name, args);
			if (m.getReturnType() != returnType) {
				throw new NoSuchMethodException("Method "+name+" does not have return type "+returnType+" - it has "+m.getReturnType());
			}
			m.setAccessible(true);
			return UnreflResult.success(desc, MethodHandles.lookup().unreflect(m));
		} catch (Throwable t) {
			return UnreflResult.failure(desc, t);
		}
	}

	private static UnreflResult unreflectConstructor(String className, Supplier<Class<?>> clazz, Class<?>... args) {
		String desc = "constructor "+className+signatureToString(args);
		try {
			Constructor<?> c = clazz.get().getDeclaredConstructor(args);
			c.setAccessible(true);
			return UnreflResult.success(desc, MethodHandles.lookup().unreflectConstructor(c));
		} catch (Throwable t) {
			return UnreflResult.failure(desc, t);
		}
	}

	private static String signatureToString(Class<?>[] args) {
		return "("+Joiner.on(", ").join(Collections2.transform(Arrays.asList(args), Class::getSimpleName))+")";
	}

	private static RuntimeException rethrow(Throwable t) {
		if (!MixinErrorHandler.actuallyItWasUs && (t instanceof MixinError || t instanceof MixinException)) {
			throw new RuntimeException("DO NOT REPORT THIS ERROR TO FABRICATION.\n"
					+ "This is caused by ANOTHER MOD'S MIXIN FAILURE that was initiated by Fabrication initializing reflection.\n"
					+ "Errors like these show up attributed to whoever was the first person to load the class with the broken mixin.\n"
					+ "!!!!! DO NOT REPORT THIS ERROR TO FABRICATION !!!!!");
		}
		if (t instanceof RuntimeException) {
			throw (RuntimeException)t;
		} else if (t instanceof Error) {
			throw (Error)t;
		} else {
			throw new RuntimeException(t);
		}
	}

	private static <T extends Throwable> RuntimeException rethrow(Throwable t, Class<T> passthru) throws T {
		if (passthru.isInstance(t)) {
			throw (T)t;
		} else {
			throw rethrow(t);
		}
	}

}
