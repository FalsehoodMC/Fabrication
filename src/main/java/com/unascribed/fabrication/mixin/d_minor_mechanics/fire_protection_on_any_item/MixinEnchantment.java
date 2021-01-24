package com.unascribed.fabrication.mixin.d_minor_mechanics.fire_protection_on_any_item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

@Mixin(Enchantment.class)
@EligibleIf(configEnabled="*.fire_protection_on_any_item")
public abstract class MixinEnchantment {

	@Inject(at=@At("HEAD"), method="isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	public void isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.fire_protection_on_any_item") && (Object)this == Enchantments.FIRE_PROTECTION && stack.getItem().isEnchantable(stack)) {
			ci.setReturnValue(true);
		}
	}
	
}
