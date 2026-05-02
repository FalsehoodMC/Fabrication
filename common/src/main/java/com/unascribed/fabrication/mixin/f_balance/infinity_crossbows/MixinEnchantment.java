package com.unascribed.fabrication.mixin.f_balance.infinity_crossbows;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
@EligibleIf(anyConfigAvailable={"*.infinity_crossbows", "*.infinity_crossbows_modded"})
public abstract class MixinEnchantment {

	@FabInject(at=@At("HEAD"), method="isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	public void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isAnyEnabled("*.infinity_crossbows") && (Object)this == Enchantments.INFINITY) {
			if (FabConf.isEnabled("*.infinity_crossbows_modded") ? stack.getItem() instanceof CrossbowItem : stack.getItem() == Items.CROSSBOW) {
				ci.setReturnValue(true);
			}
		}
	}

}
