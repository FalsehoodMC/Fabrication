package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import com.google.common.collect.ImmutableMap;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;

@Mixin(TridentEntity.class)
@EligibleIf(anyConfigEnabled={"*.tridents_accept_power", "*.tridents_accept_sharpness"})
public class MixinTridentEnchantmentsEntity {

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/enchantment/EnchantmentHelper.getAttackDamage(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityGroup;)F"),
			method="onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V")
	public float getAttackDamage(ItemStack stack, EntityGroup grp) {
		ItemStack real = stack;
		if (MixinConfigPlugin.isEnabled("*.tridents_accept_sharpness") && EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) > 0) {
			// prevents sharpness from increasing trident ranged damage, as it does in vanilla
			// this is a hacky way to do it, but it works and doesn't involve copying code out of EnchantmentHelper
			stack = real.copy();
			EnchantmentHelper.set(ImmutableMap.of(Enchantments.SHARPNESS, 0), stack);
		}
		float base = 8 + EnchantmentHelper.getAttackDamage(stack, grp);
		int power = MixinConfigPlugin.isEnabled("*.tridents_accept_power") ? EnchantmentHelper.getLevel(Enchantments.POWER, real) : 0;
		if (power > 0) {
			base *= 1 + (0.25f * (power + 1));
		}
		return base - 8;
	}
	
	
}
