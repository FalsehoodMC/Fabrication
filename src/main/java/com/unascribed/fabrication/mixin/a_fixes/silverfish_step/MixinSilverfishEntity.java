package com.unascribed.fabrication.mixin.a_fixes.silverfish_step;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.mob.SilverfishEntity;

@Mixin(SilverfishEntity.class)
@EligibleIf(configEnabled="*.silverfish_step")
public class MixinSilverfishEntity {

	// this method is poorly mapped. a better name is "hasStepSound"
	@Inject(at=@At("HEAD"), method="canClimb()Z", cancellable=true)
	public void canClimb(CallbackInfoReturnable<Boolean> ci) {
		if (RuntimeChecks.check("*.silverfish_step")) ci.setReturnValue(true);
	}
	
}
