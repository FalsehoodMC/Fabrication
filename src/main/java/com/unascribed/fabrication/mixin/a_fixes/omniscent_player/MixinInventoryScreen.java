package com.unascribed.fabrication.mixin.a_fixes.omniscent_player;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.Window;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(InventoryScreen.class)
@EligibleIf(configAvailable="*.omniscent_player", envMatches=Env.CLIENT)
public class MixinInventoryScreen {

	private static float fabrication$mouseY;

	@FabModifyVariable(method="drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V", at=@At("HEAD"), index=7, argsOnly=true)
	private static float modifyX(float value, DrawContext matrices, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, LivingEntity entity) {
		fabrication$mouseY = mouseY;
		if (!FabConf.isEnabled("*.omniscent_player")) return value;
		if (!(entity instanceof PlayerEntity)) return value;
		MinecraftClient mc = MinecraftClient.getInstance();
		Window window = mc.getWindow();
		long handle = window.getHandle();
		double[] xpos = new double[1];
		double[] ypos = new double[1];
		float scaleFactor = window.getWidth()/(float)window.getScaledWidth();
		GLFW.glfwGetCursorPos(handle, xpos, ypos);
		float curX = (float)(xpos[0]/scaleFactor);
		float curY = (float)(ypos[0]/scaleFactor);
		fabrication$mouseY = curY+(size*1.333333f);
		return curX;
	}

	@FabModifyVariable(method="drawEntity(Lnet/minecraft/client/gui/DrawContext;IIIIIFFFLnet/minecraft/entity/LivingEntity;)V", at=@At("HEAD"), index=8, argsOnly=true)
	private static float modifyY(float value) {
		return fabrication$mouseY;
	}

}
