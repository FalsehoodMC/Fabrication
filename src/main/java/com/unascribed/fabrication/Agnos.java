package com.unascribed.fabrication;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

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
		void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, boolean isDedi);
	}

	public interface TooltipRenderCallback {
		void render(ItemStack stack, List<Text> lines);
	}

	public interface HudRenderCallback {
		void render(MatrixStack matrixStack, float tickDelta);
	}

	public static void runForCommandRegistration(CommandRegistrationCallback r) {
		net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((d, reg, side) -> r.register(d, reg, side == CommandManager.RegistrationEnvironment.DEDICATED));
	}

	@Environment(EnvType.CLIENT)
	public static void runForTooltipRender(TooltipRenderCallback r) {
		ItemTooltipCallback.EVENT.register((stack, ctx, lines) -> r.render(stack, lines));
	}

	@Environment(EnvType.CLIENT)
	public static void runForHudRender(HudRenderCallback r) {
		net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(r::render);
	}

	@Environment(EnvType.CLIENT)
	public static KeyBinding registerKeyBinding(KeyBinding kb) {
		KeyBindingHelper.registerKeyBinding(kb);
		return kb;
	}

}
