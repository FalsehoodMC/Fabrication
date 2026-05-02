package com.unascribed.fabrication.mixin.b_utility.weapons_accept_silk;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.weapons_accept_silk")
public class MixinEnchantment {

	@FabInject(at=@At("HEAD"), method="isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	public void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isEnabled("*.weapons_accept_silk") && (Object)this == Enchantments.SILK_TOUCH && EnchantmentTarget.WEAPON.isAcceptableItem(stack.getItem())) {
			ci.setReturnValue(true);
		}
	}

}
