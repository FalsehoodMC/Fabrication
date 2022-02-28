package com.unascribed.fabrication.mixin.b_utility.weapons_accept_silk;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.SilkTouchEnchantment;
import net.minecraft.entity.EquipmentSlot;

@Mixin(SilkTouchEnchantment.class)
@EligibleIf(configAvailable="*.weapons_accept_silk")
public class MixinSilkTouchEnchantment extends Enchantment {

	protected MixinSilkTouchEnchantment(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
		super(weight, type, slotTypes);
	}

	@Inject(at=@At("HEAD"), method="canAccept(Lnet/minecraft/enchantment/Enchantment;)Z", cancellable=true)
	public void canAccept(Enchantment other, CallbackInfoReturnable<Boolean> ci) {
		if (!FabConf.isEnabled("*.weapons_accept_silk")) return;
		if (other == Enchantments.LOOTING) {
			ci.setReturnValue(false);
		}
	}

}
