package com.unascribed.fabrication;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.FireBlock;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
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
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class FabRefl {

	private static final boolean DEV;
	private static final boolean FORGE;
	static {
		boolean devTmp;
		boolean forgeTmp;
		try {
			Class.forName("net.minecraft.util.Identifier");
			devTmp = true;
			forgeTmp = false;
		} catch (ClassNotFoundException e) {
			devTmp = false;
			try {
				Class.forName("net.minecraft.util.ResourceLocation");
				forgeTmp = true;
			} catch (ClassNotFoundException e2) {
				forgeTmp = false;
			}
		}
		DEV = devTmp;
		FORGE = forgeTmp;
		FabLog.debug("Detected runtime: "+(DEV ? "Fabric Dev" : FORGE ? "Forge" : "Fabric"));
	}
	
	// "muh performance"
	// invokeExact becomes a standard INVOKE* insn after the JIT gets its hands on it. The entire
	// purpose of MethodHandles is to be basically free. the catch is there can be *no* abstraction
	// on an invokeExact or the calltime signature will be wrong and the JVM will get confused.
	
	private static final MethodHandle cpc2sp_channel = unreflectGetter(CustomPayloadC2SPacket.class, "channel", "field_12830", "field_149562_a");
	public static Identifier getChannel(CustomPayloadC2SPacket subject) {
		try {
			return (Identifier)cpc2sp_channel.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static final MethodHandle cpc2sp_data = unreflectGetter(CustomPayloadC2SPacket.class, "data", "field_12832", "field_149561_c");
	public static PacketByteBuf getData(CustomPayloadC2SPacket subject) {
		try {
			return (PacketByteBuf)cpc2sp_data.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static final MethodHandle feg_withinRangePredicate = unreflectGetter(FleeEntityGoal.class, "withinRangePredicate", "field_18084", "field_220872_k");
	public static TargetPredicate getWithinRangePredicate(FleeEntityGoal<?> subject) {
		try {
			return (TargetPredicate)feg_withinRangePredicate.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static final MethodHandle es_basePredicate = unreflectGetter(EntitySelector.class, "basePredicate", "field_10820", "field_197357_d");
	public static Predicate<Entity> getBasePredicate(EntitySelector subject) {
		try {
			return (Predicate<Entity>)es_basePredicate.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	private static final MethodHandle tacs_entityTrackers = unreflectGetter(ThreadedAnvilChunkStorage.class, "entityTrackers", "field_18242", "field_219272_z");
	public static Int2ObjectMap<EntityTracker> getEntityTrackers(ThreadedAnvilChunkStorage subject) {
		try {
			return (Int2ObjectMap<EntityTracker>)tacs_entityTrackers.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	private static final MethodHandle et_playersTracking = unreflectGetter(EntityTracker.class, "playersTracking", "field_18250", "field_219406_f");
	public static Set<ServerPlayerEntity> getPlayersTracking(EntityTracker subject) {
		try {
			return (Set<ServerPlayerEntity>)et_playersTracking.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	private static final MethodHandle rpm_providers_get = unreflectGetter(ResourcePackManager.class, "providers", "field_14227", "field_198987_a");
	public static Set<ResourcePackProvider> getProviders(ResourcePackManager subject) {
		try {
			return (Set<ResourcePackProvider>)rpm_providers_get.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	private static final MethodHandle rpm_providers_set = unreflectSetter(ResourcePackManager.class, "providers", "field_14227", "field_198987_a");
	public static void setProviders(ResourcePackManager subject, Set<ResourcePackProvider> providers) {
		try {
			rpm_providers_set.invokeExact(subject, providers);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static final MethodHandle abs_hardness_get = unreflectGetter(AbstractBlockState.class, "hardness", "field_23172", "field_235705_i_");
	public static float getHardness(AbstractBlockState subject) {
		try {
			return (float)abs_hardness_get.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static final MethodHandle abs_hardness_set = unreflectSetter(AbstractBlockState.class, "hardness", "field_23172", "field_235705_i_");
	public static void setHardness(AbstractBlockState subject, float hardness) {
		try {
			abs_hardness_set.invokeExact(subject, hardness);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static final MethodHandle fb_burnChances = unreflectGetter(FireBlock.class, "burnChances", "field_11095", "field_149849_a");
	public static Object2IntMap<Block> getBurnChances(FireBlock subject) {
		try {
			return (Object2IntMap<Block>)fb_burnChances.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static final MethodHandle fb_spreadChances = unreflectGetter(FireBlock.class, "spreadChances", "field_11091", "field_149848_b");
	public static Object2IntMap<Block> getSpreadChances(FireBlock subject) {
		try {
			return (Object2IntMap<Block>)fb_spreadChances.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static final MethodHandle dc_color = unreflectGetter(DyeColor.class, "color", "field_7949", "field_193351_w");
	public static int getColor(DyeColor subject) {
		try {
			return (int)dc_color.invokeExact(subject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	
	private static final MethodHandle gc_execute = unreflectMethod(GiveCommand.class, "execute", "method_13401", "func_198497_a",
			int.class,
			ServerCommandSource.class, ItemStackArgument.class, Collection.class, int.class);
	public static int GiveCommand_execute(ServerCommandSource source, ItemStackArgument item, Collection<ServerPlayerEntity> targets, int count) throws CommandSyntaxException {
		try {
			return (int)gc_execute.invokeExact(source, item, targets, count);
		} catch (Throwable t) {
			throw rethrow(t, CommandException.class);
		}
	}
	
	private static final MethodHandle me_getDropChance = unreflectMethod(MobEntity.class, "getDropChance", "method_5929", "func_205712_c",
			float.class,
			EquipmentSlot.class);
	public static float MobEntity_getDropChance(MobEntity subject, EquipmentSlot slot) {
		try {
			return (float)me_getDropChance.invokeExact(subject, slot);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static final MethodHandle fb_registerFlammableBlock = unreflectMethod(FireBlock.class,
			"registerFlammableBlock", "method_10189", "func_180686_a",
			void.class,
			Block.class, int.class, int.class);
	public static void FireBlock_registerFlammableBlock(FireBlock subject, Block block, int burnChance, int spreadChance) {
		try {
			fb_registerFlammableBlock.invokeExact(subject, block, burnChance, spreadChance);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	@Environment(EnvType.CLIENT)
	public static final class Client {
	
		private static final MethodHandle satd_width = unreflectGetter(SpriteAtlasTexture.Data.class, "width", "field_17901", "field_217806_b");
		public static int getWidth(SpriteAtlasTexture.Data subject) {
			try {
				return (int)satd_width.invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		private static final MethodHandle satd_height = unreflectGetter(SpriteAtlasTexture.Data.class, "height", "field_17902", "field_217807_c");
		public static int getHeight(SpriteAtlasTexture.Data subject) {
			try {
				return (int)satd_height.invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}
		
		private static final MethodHandle satd_maxLevel = unreflectGetter(SpriteAtlasTexture.Data.class, "maxLevel", "field_21795", "field_229224_d_");
		public static int getMaxLevel(SpriteAtlasTexture.Data subject) {
			try {
				return (int)satd_maxLevel.invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		private static final MethodHandle sprite_x = unreflectGetter(Sprite.class, "x", "field_5258", "field_110975_c");
		public static int getX(Sprite subject) {
			try {
				return (int)sprite_x.invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}
		
		private static final MethodHandle sprite_y = unreflectGetter(Sprite.class, "y", "field_5256", "field_110974_d");
		public static int getY(Sprite subject) {
			try {
				return (int)sprite_y.invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}
		
		private static final MethodHandle sprite_frameIndex = unreflectGetter(Sprite.class, "frameIndex", "field_5273", "field_110973_g");
		public static int getFrameIndex(Sprite subject) {
			try {
				return (int)sprite_frameIndex.invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}
		
		private static final MethodHandle sprite_frameTicks = unreflectGetter(Sprite.class, "frameTicks", "field_5272", "field_110983_h");
		public static int getFrameTicks(Sprite subject) {
			try {
				return (int)sprite_frameTicks.invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		private static final MethodHandle sat_sprites = unreflectGetter(SpriteAtlasTexture.class, "sprites", "field_5280", "field_94252_e");
		public static Map<Identifier, Sprite> getSprites(SpriteAtlasTexture subject) {
			try {
				return (Map<Identifier, Sprite>)sat_sprites.invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}

		private static final MethodHandle sat_animatedSprites = unreflectGetter(SpriteAtlasTexture.class, "animatedSprites", "field_5276", "field_94258_i");
		public static List<Sprite> getAnimatedSprites(SpriteAtlasTexture subject) {
			try {
				return (List<Sprite>)sat_animatedSprites.invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}
		
		private static final MethodHandle mc_itemColors = unreflectGetter(MinecraftClient.class, "itemColors", "field_1760", "field_184128_aI");
		public static ItemColors getItemColors(MinecraftClient subject) {
			try {
				return (ItemColors)mc_itemColors.invokeExact(subject);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}
		
		
		
		private static final MethodHandle mh_blend = unreflectMethod(MipmapHelper.class, "blend", "method_24101", "func_229172_a_",
				int.class,
				int.class, int.class, int.class, int.class, boolean.class);
		public static int MipmapHelper_blend(int one, int two, int three, int four, boolean checkAlpha) {
			try {
				return (int)mh_blend.invokeExact(one, two, three, four, checkAlpha);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}
		
		private static final MethodHandle ni_new = unreflectConstructor(NativeImage.class,
				Format.class, int.class, int.class, boolean.class, long.class);
		public static NativeImage NativeImage_new(Format format, int width, int height, boolean useStb, long pointer) {
			try {
				return (NativeImage)ni_new.invokeExact(format, width, height, useStb, pointer);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}
		
		private static final MethodHandle ir_renderBakedItemModel = unreflectMethod(ItemRenderer.class, "renderBakedItemModel", "method_23182", "func_229114_a_",
				void.class,
				BakedModel.class, ItemStack.class, int.class, int.class, MatrixStack.class, VertexConsumer.class);
		public static void ItemRenderer_renderBakedItemModel(ItemRenderer subject, BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
			try {
				ir_renderBakedItemModel.invokeExact(subject, model, stack, light, overlay, matrices, vertices);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}
		
	}

	private static MethodHandle unreflectGetter(Class<?> clazz, String yarnName, String interName, String srgName) {
		try {
			String name = DEV ? yarnName : FORGE ? srgName : interName;
			Field f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			return MethodHandles.lookup().unreflectGetter(f);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static MethodHandle unreflectSetter(Class<?> clazz, String yarnName, String interName, String srgName) {
		try {
			String name = DEV ? yarnName : FORGE ? srgName : interName;
			Field f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			return MethodHandles.lookup().unreflectSetter(f);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static MethodHandle unreflectMethod(Class<?> clazz, String yarnName, String interName, String srgName, Class<?> returnType, Class<?>... args) {
		try {
			String name = DEV ? yarnName : FORGE ? srgName : interName;
			Method m = clazz.getDeclaredMethod(name, args);
			if (m.getReturnType() != returnType) {
				throw new NoSuchMethodException("Method "+name+" does not have return type "+returnType+" - it has "+m.getReturnType());
			}
			m.setAccessible(true);
			return MethodHandles.lookup().unreflect(m);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}
	
	private static MethodHandle unreflectConstructor(Class<?> clazz, Class<?>... args) {
		try {
			Constructor<?> c = clazz.getDeclaredConstructor(args);
			c.setAccessible(true);
			return MethodHandles.lookup().unreflectConstructor(c);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	private static RuntimeException rethrow(Throwable t) {
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
