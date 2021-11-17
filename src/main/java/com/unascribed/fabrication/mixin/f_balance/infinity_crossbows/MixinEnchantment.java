package com.unascribed.fabrication.mixin.f_balance.infinity_crossbows;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.infinity_crossbows")
public abstract class MixinEnchantment {

	@Inject(at=@At("HEAD"), method="isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	public void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.infinity_crossbows") && (Object)this == Enchantments.INFINITY && stack.getItem() == Items.CROSSBOW) {
			ci.setReturnValue(true);
		}
	}

}
