package com.unascribed.fabrication.mixin.g_weird_tweaks.no_dolphin_theft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

@Mixin(targets="net.minecraft.entity.passive.DolphinEntity$PlayWithItemsGoal")
@EligibleIf(configAvailable="*.no_dolphin_theft")
public abstract class MixinPlayWithItemsGoal {

	@Inject(at=@At("HEAD"), method="canStart()Z", cancellable=true)
	private void preventTheft(CallbackInfoReturnable<Boolean> cir) {
		if (MixinConfigPlugin.isEnabled("*.no_dolphin_theft")) {
			cir.setReturnValue(false);
		}
	}

}
