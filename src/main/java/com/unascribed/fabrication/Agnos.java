package com.unascribed.fabrication;

import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.support.FabricationEvents;
import com.unascribed.fabrication.support.FabricationEventsClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
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
		void render(DrawContext drawContext, float tickDelta);
	}

	public static void runForCommandRegistration(CommandRegistrationCallback r) {
		FabricationEvents.addCommand(r);
	}

	@Environment(EnvType.CLIENT)
	public static void runForTooltipRender(TooltipRenderCallback r) {
		FabricationEventsClient.addTooltip(r);
	}

	@Environment(EnvType.CLIENT)
	public static void runForHudRender(HudRenderCallback r) {
		FabricationEventsClient.addHud(r);
	}

	@Environment(EnvType.CLIENT)
	public static void registerKeyBinding(KeyBinding kb) {
		FabricationEventsClient.addKeybind(kb);
	}

}
