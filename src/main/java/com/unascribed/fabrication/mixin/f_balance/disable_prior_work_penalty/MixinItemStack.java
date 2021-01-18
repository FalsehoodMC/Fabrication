package com.unascribed.fabrication.mixin.f_balance.disable_prior_work_penalty;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

@Mixin(ItemStack.class)
@EligibleIf(configEnabled="*.disable_prior_work_penalty")
public class MixinItemStack {

	@Inject(at=@At("HEAD"), method="getRepairCost()I", cancellable=true, expect=1)
	public void getRepairCost(CallbackInfoReturnable<Integer> cir) {
		if (!MixinConfigPlugin.isEnabled("*.disable_prior_work_penalty")) return;
		cir.setReturnValue(0);
	}
	
}
