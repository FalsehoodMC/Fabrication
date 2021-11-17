package com.unascribed.fabrication.mixin.i_woina.no_experience;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(InGameHud.class)
@EligibleIf(configAvailable="*.no_experience", envMatches=Env.CLIENT)
public class MixinInGameHud {

	@Inject(at=@At("HEAD"), method="renderExperienceBar(Lnet/minecraft/client/util/math/MatrixStack;I)V", cancellable=true)
	public void renderExperienceBar(MatrixStack matrices, int i, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.no_experience")) {
			ci.cancel();
		}
	}

	@Inject(at=@At("HEAD"), method="renderStatusBars(Lnet/minecraft/client/util/math/MatrixStack;)V")
	private void renderStatusBarsHead(MatrixStack matrices, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.no_experience")) {
			matrices.push();
			matrices.translate(0, 7, 0);
		}
	}

	@Inject(at=@At("RETURN"), method="renderStatusBars(Lnet/minecraft/client/util/math/MatrixStack;)V")
	private void renderStatusBarsRet(MatrixStack matrices, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.no_experience")) {
			matrices.pop();
		}
	}

}
