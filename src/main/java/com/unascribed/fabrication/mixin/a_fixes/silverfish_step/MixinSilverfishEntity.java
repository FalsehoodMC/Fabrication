package com.unascribed.fabrication.mixin.a_fixes.silverfish_step;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.mob.SilverfishEntity;

@Mixin(SilverfishEntity.class)
@EligibleIf(configAvailable="*.silverfish_step")
public class MixinSilverfishEntity {

	// this method is poorly mapped. a better name is "hasStepSound"
	@FabInject(at=@At("HEAD"), method="canClimb()Z", cancellable=true)
	public void canClimb(CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isEnabled("*.silverfish_step")) ci.setReturnValue(true);
	}

}
