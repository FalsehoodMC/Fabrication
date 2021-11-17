package com.unascribed.fabrication.mixin.c_tweaks.no_sneak_bypass;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.Entity;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.no_sneak_bypass")
public class MixinEntity {

	@Inject(at=@At("HEAD"), method="bypassesSteppingEffects()Z", cancellable=true)
	public void bypassesSteppingEffects(CallbackInfoReturnable<Boolean> cir) {
		if (MixinConfigPlugin.isEnabled("*.no_sneak_bypass") && ConfigPredicates.shouldRun("*.no_sneak_bypass", (Entity)(Object)this)) {
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
