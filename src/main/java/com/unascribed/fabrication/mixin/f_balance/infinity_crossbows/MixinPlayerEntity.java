package com.unascribed.fabrication.mixin.f_balance.infinity_crossbows;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.infinity_crossbows")
public class MixinPlayerEntity {

	@Redirect(at=@At(value="FIELD", target="net/minecraft/entity/player/PlayerAbilities.creativeMode:Z"),
			method="getArrowType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;")
	public boolean redirectIsCreativeMode(PlayerAbilities subject, ItemStack crossbow) {
		if (FabConf.isEnabled("*.infinity_crossbows") && FabConf.isEnabled("*.infibows")
				&& EnchantmentHelper.getLevel(Enchantments.INFINITY, crossbow) > 0) {
			return true;
		}
		return subject.creativeMode;
	}

}
