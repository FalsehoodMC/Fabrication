package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.InfinityEnchantment;
import net.minecraft.enchantment.MendingEnchantment;
import net.minecraft.entity.EquipmentSlot;

@Mixin(InfinityEnchantment.class)
@EligibleIf(configEnabled="*.infinity_mending")
public abstract class MixinInfinityMending extends Enchantment {

	protected MixinInfinityMending(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
		super(weight, type, slotTypes);
	}
	
	@Inject(at=@At("HEAD"), method="canAccept(Lnet/minecraft/enchantment/Enchantment;)Z", cancellable=true)
	public void canAccept(Enchantment other, CallbackInfoReturnable<Boolean> ci) {
		if (RuntimeChecks.check("*.infinity_mending") && other instanceof MendingEnchantment) {
			ci.setReturnValue(true);
		}
	}

}
