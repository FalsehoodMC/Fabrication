package com.unascribed.fabrication.mixin.z_combined.trident_enchantments;

import com.unascribed.fabrication.support.injection.ModifyReturn;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;

@Mixin(TridentEntity.class)
@EligibleIf(anyConfigAvailable={"*.tridents_accept_power", "*.tridents_accept_sharpness", "*.bedrock_impaling"})
public class MixinTridentEntity {

	@ModifyReturn(target={"Lnet/minecraft/enchantment/EnchantmentHelper;getAttackDamage(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityGroup;)F", "Lnet/minecraft/class_1890;method_8218(Lnet/minecraft/class_1799;Lnet/minecraft/class_1310;)F"},
			method={"onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V", "method_7454(Lnet/minecraft/class_3966;)V"})
	private static float fabrication$modifyAttackDamage(float damage, ItemStack stack, EntityGroup grp, TridentEntity self, EntityHitResult ehr) {
		if (MixinConfigPlugin.isEnabled("*.tridents_accept_sharpness")) {
			int sharpness = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack);
			if (sharpness > 0) {
				damage -= Enchantments.SHARPNESS.getAttackDamage(sharpness, grp);
			}
		}
		if (MixinConfigPlugin.isEnabled("*.bedrock_impaling") && grp != EntityGroup.AQUATIC) {
			int impaling = EnchantmentHelper.getLevel(Enchantments.IMPALING, stack);
			if (impaling > 0 && ehr.getEntity().isWet()) {
				damage += Enchantments.IMPALING.getAttackDamage(impaling, EntityGroup.AQUATIC);
			}
		}
		if (MixinConfigPlugin.isEnabled("*.tridents_accept_power")) {
			int power = EnchantmentHelper.getLevel(Enchantments.POWER, stack);
			if (power > 0) {
				damage *= 1 + (0.25f * (power + 1));
			}
		}
		return damage;
	}


}
