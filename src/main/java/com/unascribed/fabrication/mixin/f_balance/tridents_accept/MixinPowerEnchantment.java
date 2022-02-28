package com.unascribed.fabrication.mixin.f_balance.tridents_accept;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.ImpalingEnchantment;
import net.minecraft.enchantment.PowerEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(PowerEnchantment.class)
@EligibleIf(configAvailable="*.tridents_accept_power")
public class MixinPowerEnchantment extends Enchantment {

	// enchantment is implemented in MixinTridentEntity in z_combined.trident_enchantments

	protected MixinPowerEnchantment(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
		super(weight, type, slotTypes);
	}

	@Override
	public boolean canAccept(Enchantment other) {
		return !(other instanceof DamageEnchantment || other instanceof ImpalingEnchantment) && super.canAccept(other);
	}

	@Override
	public boolean isAcceptableItem(ItemStack stack) {
		return (FabConf.isEnabled("*.tridents_accept_power") && stack.getItem() == Items.TRIDENT) || super.isAcceptableItem(stack);
	}

}
