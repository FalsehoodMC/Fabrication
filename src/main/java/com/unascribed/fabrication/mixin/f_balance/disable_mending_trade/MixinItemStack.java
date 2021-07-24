package com.unascribed.fabrication.mixin.f_balance.disable_mending_trade;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.MendingEnchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
@EligibleIf(configEnabled="*.disable_mending_trade")
public class MixinItemStack {

	@Inject(at=@At("HEAD"), method= "isAvailableForEnchantedBookOffer()Z", cancellable=true)
	public void getRepairCost(CallbackInfoReturnable<Boolean> cir) {
		if (MixinConfigPlugin.isEnabled("*.disable_mending_trade") && ((Object)this) instanceof MendingEnchantment)
			cir.setReturnValue(false);
	}
	
}
