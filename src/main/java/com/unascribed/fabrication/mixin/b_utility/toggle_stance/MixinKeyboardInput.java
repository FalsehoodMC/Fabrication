package com.unascribed.fabrication.mixin.b_utility.toggle_stance;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.features.FeatureToggleStance;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.input.KeyboardInput;

@Mixin(KeyboardInput.class)
@EligibleIf(configAvailable="*.toggle_stance", envMatches=Env.CLIENT)
public class MixinKeyboardInput {

	@Inject(at=@At("TAIL"), method="tick(Z)V")
	public void tick(boolean slowDown, CallbackInfo ci) {
		if (FabConf.isEnabled("*.toggle_stance") && FeatureToggleStance.currentStance.sneaking) {
			((KeyboardInput)(Object)this).sneaking = true;
		}
	}

}
