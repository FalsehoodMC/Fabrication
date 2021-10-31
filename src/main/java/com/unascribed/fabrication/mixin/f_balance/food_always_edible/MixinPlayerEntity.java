package com.unascribed.fabrication.mixin.f_balance.food_always_edible;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import com.unascribed.fabrication.support.ConfigPredicates;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.food_always_edible", modNotLoaded="eternaleats")
public class MixinPlayerEntity {
	
	@Inject(method="canConsume(Z)Z", at=@At("HEAD"), cancellable=true)
	public void canConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
		if (!MixinConfigPlugin.isEnabled("*.food_always_edible")) return;
		if (!ConfigPredicates.shouldRun("", (PlayerEntity)(Object)this)) return;
		cir.setReturnValue(true);
	}
	
}
