package com.unascribed.fabrication.mixin.z_combined.trident_enchantments;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;

import com.google.common.collect.ImmutableMap;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;

@Mixin(TridentEntity.class)
@EligibleIf(anyConfigAvailable={"*.tridents_accept_power", "*.tridents_accept_sharpness", "*.bedrock_impaling"})
public class MixinTridentEntity {

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/enchantment/EnchantmentHelper.getAttackDamage(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityGroup;)F"),
			method="onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V")
	public float getAttackDamage(ItemStack stack, EntityGroup grp, EntityHitResult ehr) {
		ItemStack real = stack;
		if (FabConf.isEnabled("*.tridents_accept_sharpness") && EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack) > 0) {
			// prevents sharpness from increasing trident ranged damage, as it does in vanilla
			// this is a hacky way to do it, but it works and doesn't involve copying code out of EnchantmentHelper
			stack = real.copy();
			EnchantmentHelper.set(ImmutableMap.of(Enchantments.SHARPNESS, 0), stack);
		}
		if (FabConf.isEnabled("*.bedrock_impaling") && EnchantmentHelper.getLevel(Enchantments.IMPALING, stack) > 0 && ehr.getEntity().isWet()) {
			grp = EntityGroup.AQUATIC;
		}
		float base = 8 + EnchantmentHelper.getAttackDamage(stack, grp);
		int power = FabConf.isEnabled("*.tridents_accept_power") ? EnchantmentHelper.getLevel(Enchantments.POWER, real) : 0;
		if (power > 0) {
			base *= 1 + (0.25f * (power + 1));
		}
		return base - 8;
	}


}
