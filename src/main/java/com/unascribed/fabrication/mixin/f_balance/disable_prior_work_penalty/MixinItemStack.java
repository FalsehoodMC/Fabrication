package com.unascribed.fabrication.mixin.f_balance.disable_prior_work_penalty;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
@EligibleIf(anyConfigAvailable={"*.disable_prior_work_penalty", "*.anvil_no_xp_cost"})
public class MixinItemStack {

	@FabInject(at=@At("HEAD"), method="getRepairCost()I", cancellable=true)
	public void getRepairCost(CallbackInfoReturnable<Integer> cir) {
		if (!(FabConf.isEnabled("*.disable_prior_work_penalty") || FabConf.isEnabled("*.anvil_no_xp_cost"))) return;
		cir.setReturnValue(0);
	}

}
