package com.unascribed.fabrication.mixin.h_situational.weapons_accept_silk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.weapons_accept_silk")
public class MixinEnchantment {

	@Inject(at=@At("HEAD"), method="isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	public void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.weapons_accept_silk") && (Object)this == Enchantments.SILK_TOUCH && EnchantmentTarget.WEAPON.isAcceptableItem(stack.getItem())) {
			ci.setReturnValue(true);
		}
	}
	
}
