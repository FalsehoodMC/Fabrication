package com.unascribed.fabrication.mixin.d_minor_mechanics.infibows;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value=CrossbowItem.class, priority=1001)
@EligibleIf(anyConfigAvailable="*.infibows")
public class MixinCrossbowItem {

	@FabModifyVariable(at=@At("HEAD"), method="loadProjectile(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;ZZ)Z",
			argsOnly=true, index=2)
	private static ItemStack fabrication$modifyCreativeModeLoadProjectile(ItemStack projectile, LivingEntity shooter, ItemStack crossbow, ItemStack p, boolean sim, boolean creative) {
		if (FabConf.isAnyEnabled("*.infibows") && EnchantmentHelper.getLevel(Enchantments.INFINITY, crossbow) > 0 && projectile.isEmpty()) {
			return Items.ARROW.getDefaultStack();
		}
		return projectile;
	}

}
