package com.unascribed.fabrication.mixin.z_combined.trident_enchantments;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.bedrock_impaling")
public class MixinPlayerEntity {
	@ModifyReturn(method="attack(Lnet/minecraft/entity/Entity;)V", target="Lnet/minecraft/enchantment/EnchantmentHelper;getAttackDamage(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityGroup;)F")
	private static float fabrication$impalling$modifyAttackDamage(float old, ItemStack stack, EntityGroup grp, PlayerEntity self, Entity target) {
		if (FabConf.isEnabled("*.bedrock_impaling") && grp != EntityGroup.AQUATIC) {
			int impaling = EnchantmentHelper.getLevel(Enchantments.IMPALING, stack);
			if (impaling > 0 && target.isWet()) {
				return old + Enchantments.IMPALING.getAttackDamage(impaling, EntityGroup.AQUATIC);
			}
		}
		return old;
	}
}
