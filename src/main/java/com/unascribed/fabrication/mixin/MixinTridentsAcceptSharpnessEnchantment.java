package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.ImpalingEnchantment;
import net.minecraft.enchantment.PowerEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(DamageEnchantment.class)
@EligibleIf(configEnabled="*.tridents_accept_sharpness")
public class MixinTridentsAcceptSharpnessEnchantment extends Enchantment {

	protected MixinTridentsAcceptSharpnessEnchantment(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
		super(weight, type, slotTypes);
	}

	@Override
	public boolean canAccept(Enchantment other) {
		return !(other instanceof PowerEnchantment || other instanceof ImpalingEnchantment) && super.canAccept(other);
	}
	
	@Override
	public boolean isAcceptableItem(ItemStack stack) {
		return (RuntimeChecks.check("*.tridents_accept_sharpness") && this == Enchantments.SHARPNESS && stack.getItem() == Items.TRIDENT)
				|| super.isAcceptableItem(stack);
	}
	
}
