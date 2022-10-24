package com.unascribed.fabrication.mixin.e_mechanics.bottled_air;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

@Mixin(GlassBottleItem.class)
@EligibleIf(configAvailable="*.bottled_air")
public class MixinGlassBottleItem {

	@FabInject(at=@At("HEAD"), method="fill(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;")
	protected void fill(ItemStack empty, PlayerEntity player, ItemStack filled, CallbackInfoReturnable<ItemStack> ci) {
		if (FabConf.isEnabled("*.bottled_air") && player.isSubmergedInWater()) {
			if (empty.getItem() == Items.GLASS_BOTTLE && PotionUtil.getPotion(filled) == Potions.WATER) {
				if (player.getAir() < player.getMaxAir()) {
					player.setAir(Math.min(player.getMaxAir(), player.getAir()+30));
				}
			}
		}
	}

}
