package com.unascribed.fabrication;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

// Forge implementation of Agnos. For linguistic and philosophical waffling, see the Fabric version.
public final class Agnos {

	public interface CommandRegistrationCallback {
		void register(CommandDispatcher<ServerCommandSource> commandDispatcher, boolean dedicated);
	}

	public interface TooltipRenderCallback {
		void render(ItemStack stack, List<Text> lines);
	}

	public interface HudRenderCallback {
		void render(MatrixStack matrixStack, float tickDelta);
	}

	public static void runForCommandRegistration(CommandRegistrationCallback r) {
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> {
			r.register(e.getDispatcher(), e.getEnvironment() == CommandManager.RegistrationEnvironment.DEDICATED);
		});
	}

	@OnlyIn(Dist.CLIENT)
	public static void runForTooltipRender(TooltipRenderCallback r) {
		MinecraftForge.EVENT_BUS.addListener((ItemTooltipEvent e) -> {
			r.render(e.getItemStack(), e.getToolTip());
		});
	}

	@OnlyIn(Dist.CLIENT)
	public static void runForHudRender(HudRenderCallback r) {
		MinecraftForge.EVENT_BUS.addListener((RenderGameOverlayEvent.Post e) -> {
			if (e.getType() == ElementType.ALL) {
				r.render(e.getMatrixStack(), e.getPartialTicks());
			}
		});
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerKeyBinding(KeyBinding kb) {
		//cool a API, with conflicting defaults
		//kb.setKeyConflictContext(KeyConflictContext.IN_GAME);
		ClientRegistry.registerKeyBinding(kb);
	}
}
