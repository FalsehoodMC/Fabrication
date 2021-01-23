package com.unascribed.fabrication;

import java.nio.file.Path;
import java.util.List;

import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.support.Env;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * "Agnos" can mean many things. It's a given name, a shortening of "agnostic", Aromanian for "disgust",
 * Latin for "lamb" (especially of the sacrificial variety), and Tagalog for "holy relic" (but it is
 * borrowed from a Latin phrase referring to the Latin meaning).
 * <p>
 * Agnos is all of these things. It's a name for a class (all classes need names), an
 * abstraction for <i>loader agnosticism</i>, disgusting (I don't like Forge), and this
 * implementation is a sacrificial lamb, replaced with an alternative Forge version by a patcher.
 */
public final class Agnos {

	public interface CommandRegistrationCallback {
		void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated);
	}
	
	public interface TooltipRenderCallback {
		void render(ItemStack stack, List<Text> lines);
	}
	
	public static void runForCommandRegistration(CommandRegistrationCallback r) {
		net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register(r::register);
	}
	
	public static void runForTooltipRender(TooltipRenderCallback r) {
		ItemTooltipCallback.EVENT.register((stack, ctx, lines) -> r.render(stack, lines));
	}
	
	public static <T> T registerSoundEvent(String id, T soundEvent) {
		return (T)Registry.register(Registry.SOUND_EVENT, new Identifier(id), (SoundEvent)soundEvent);
	}
	
	public static <T> T registerBlockTag(String id) {
		return (T)TagRegistry.block(new Identifier(id));
	}

	public static <T> T registerItemTag(String id) {
		return (T)TagRegistry.item(new Identifier(id));
	}
	
	public static boolean eventsAvailable() {
		return FabricLoader.getInstance().isModLoaded("fabric");
	}

	public static Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	public static Env getCurrentEnv() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	public static boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}
	
	public static String getModVersion() {
		return FabricLoader.getInstance().getModContainer("fabrication").get().getMetadata().getVersion().getFriendlyString();
	}
	
	public static byte[] getClassBytes(Class<?> clazz) {
		try {
			byte[] bys = FabricLauncherBase.getLauncher().getClassByteArray(clazz.getName(), true);
			((IMixinTransformer)MixinEnvironment.getCurrentEnvironment().getActiveTransformer()).transformClassBytes(clazz.getName(), clazz.getName(), bys);
			return bys;
		} catch (Throwable e) {
			FabLog.warn("Failed to look up "+clazz, e);
			return null;
		}
	}
	
}
