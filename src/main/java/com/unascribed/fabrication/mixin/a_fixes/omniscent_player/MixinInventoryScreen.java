package com.unascribed.fabrication.mixin.a_fixes.omniscent_player;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.Window;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(InventoryScreen.class)
@EligibleIf(configEnabled="*.omniscent_player", envMatches=Env.CLIENT)
public class MixinInventoryScreen {
	
	private static boolean fabrication$reentering = false;
	
	@Inject(at=@At("HEAD"), method="drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V", cancellable=true)
	private static void drawEntity(int x, int y, int size, float mouseX, float mouseY, LivingEntity entity, CallbackInfo ci) {
		if (!RuntimeChecks.check("*.omniscent_player")) return;
		if (fabrication$reentering) return;
		if (!(entity instanceof PlayerEntity)) return;
		try {
			fabrication$reentering = true;
			MinecraftClient mc = MinecraftClient.getInstance();
			Window window = mc.getWindow();
			long handle = FabricationMod.snag(Window.class, window, "field_5187", "handle");
			double[] xpos = new double[1];
			double[] ypos = new double[1];
			float scaleFactor = window.getWidth()/(float)window.getScaledWidth();
			GLFW.glfwGetCursorPos(handle, xpos, ypos);
			float curX = (float)(xpos[0]/scaleFactor);
			float curY = (float)(ypos[0]/scaleFactor);
			InventoryScreen.drawEntity(x, y, size, x-curX, y-curY-(size*1.333333f), entity);
			ci.cancel();
		} finally {
			fabrication$reentering = false;
		}
	}
	
}
