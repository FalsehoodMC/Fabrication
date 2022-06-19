package com.unascribed.fabrication.mixin.g_weird_tweaks.endermen_dont_grief;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

@Mixin(targets = {"net.minecraft.entity.mob.EndermanEntity$PickUpBlockGoal","net.minecraft.entity.mob.EndermanEntity$PlaceBlockGoal"})
@EligibleIf(configAvailable="*.endermen_dont_grief")
public class MixinEndermanEntity {
	@FabInject(method = "canStart()Z", at = @At("HEAD"), cancellable = true)
	private void canStart(CallbackInfoReturnable<Boolean> info) {
		if (FabConf.isEnabled("*.endermen_dont_grief")) {
			info.setReturnValue(false);
		}
	}
}
