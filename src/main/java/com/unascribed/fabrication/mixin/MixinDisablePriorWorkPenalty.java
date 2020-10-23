package com.unascribed.fabrication.mixin;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

@Mixin(ItemStack.class)
@EligibleIf(configEnabled="*.disable_prior_work_penalty")
public class MixinDisablePriorWorkPenalty {

	@Inject(at=@At("HEAD"), method="getRepairCost()I", cancellable=true)
	public void getRepairCost(CallbackInfoReturnable<Integer> cir) {
		if (!RuntimeChecks.check("*.disable_prior_work_penalty")) return;
		cir.setReturnValue(0);
	}
	
}
