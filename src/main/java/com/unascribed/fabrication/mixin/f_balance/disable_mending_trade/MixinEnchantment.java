package com.unascribed.fabrication.mixin.f_balance.disable_mending_trade;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.MendingEnchantment;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.disable_mending_trade")
public class MixinEnchantment {

	@Inject(at=@At("HEAD"), method= "isAvailableForEnchantedBookOffer()Z", cancellable=true)
	public void getRepairCost(CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.disable_mending_trade") && ((Object)this) instanceof MendingEnchantment)
			cir.setReturnValue(false);
	}

}
