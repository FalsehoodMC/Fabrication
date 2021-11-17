package com.unascribed.fabrication.mixin.a_fixes.omniscent_player;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.Window;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(InventoryScreen.class)
@EligibleIf(configAvailable="*.omniscent_player", envMatches=Env.CLIENT)
public class MixinInventoryScreen {

	private static float fabrication$mouseY;

	@ModifyVariable(method="drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V", at=@At("HEAD"), index=3, argsOnly=true)
	private static float modifyX(float value, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity) {
		fabrication$mouseY = mouseY;
		if (!MixinConfigPlugin.isEnabled("*.omniscent_player")) return value;
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
		fabrication$mouseY = y-curY-(size*1.333333f);
		return x-curX;
	}

	@ModifyVariable(method="drawEntity(IIIFFLnet/minecraft/entity/LivingEntity;)V", at=@At("HEAD"), index=4, argsOnly=true)
	private static float modifyY(float value) {
		return fabrication$mouseY;
	}

}
