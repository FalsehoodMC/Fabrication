package com.unascribed.fabrication.mixin.g_weird_tweaks.no_dolphin_theft;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

@Mixin(targets="net.minecraft.entity.passive.DolphinEntity$PlayWithItemsGoal")
@EligibleIf(configAvailable="*.no_dolphin_theft")
public abstract class MixinPlayWithItemsGoal {

	@FabInject(at=@At("HEAD"), method="canStart()Z", cancellable=true)
	private void preventTheft(CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.no_dolphin_theft")) {
			cir.setReturnValue(false);
		}
	}

}
