package com.unascribed.fabrication;

import java.nio.file.Path;
import java.util.List;

import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.support.Env;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
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
	
	public interface HudRenderCallback {
		void render(MatrixStack matrixStack, float tickDelta);
	}
	
	public static void runForCommandRegistration(CommandRegistrationCallback r) {
		net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register(r::register);
	}
	
	@Environment(EnvType.CLIENT)
	public static void runForTooltipRender(TooltipRenderCallback r) {
		ItemTooltipCallback.EVENT.register((stack, ctx, lines) -> r.render(stack, lines));
	}
	
	@Environment(EnvType.CLIENT)
	public static void runForHudRender(HudRenderCallback r) {
		net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((ms, d) -> r.render(ms, d));
	}
	
	public static SoundEvent registerSoundEvent(Identifier id, SoundEvent soundEvent) {
		return Registry.register(Registry.SOUND_EVENT, id, soundEvent);
	}
	
	@Environment(EnvType.CLIENT)
	public static KeyBinding registerKeyBinding(KeyBinding kb) {
		KeyBindingHelper.registerKeyBinding(kb);
		return kb;
	}
	
	public static boolean eventsAvailable() {
		return true;
	}

	public static Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	public static Env getCurrentEnv() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	public static boolean isModLoaded(String modid) {
		if (modid.startsWith("forge:")) return false;
		if (modid.startsWith("fabric:")) modid = modid.substring(7);
		return FabricLoader.getInstance().isModLoaded(modid);
	}
	
	public static String getModVersion() {
		if (FabRefl.DEV) return "DEV";
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

	public static String getLoaderVersion() {
		return FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString();
	}
	
}
