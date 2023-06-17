package com.unascribed.fabrication.mixin.i_woina.no_experience;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(InGameHud.class)
@EligibleIf(configAvailable="*.no_experience", envMatches=Env.CLIENT)
public class MixinInGameHud {

	@FabInject(at=@At("HEAD"), method="renderExperienceBar(Lnet/minecraft/client/gui/DrawContext;I)V", cancellable=true)
	public void renderExperienceBar(DrawContext matrices, int i, CallbackInfo ci) {
		if (FabConf.isEnabled("*.no_experience")) {
			ci.cancel();
		}
	}

	@FabInject(at=@At("HEAD"), method="renderStatusBars(Lnet/minecraft/client/gui/DrawContext;)V")
	private void renderStatusBarsHead(DrawContext drawContext, CallbackInfo ci) {
		if (FabConf.isEnabled("*.no_experience")) {
			MatrixStack matrices = drawContext.getMatrices();
			matrices.push();
			matrices.translate(0, 7, 0);
		}
	}

	@FabInject(at=@At("RETURN"), method="renderStatusBars(Lnet/minecraft/client/gui/DrawContext;)V")
	private void renderStatusBarsRet(DrawContext drawContext, CallbackInfo ci) {
		if (FabConf.isEnabled("*.no_experience")) {
			drawContext.getMatrices().pop();
		}
	}

}
