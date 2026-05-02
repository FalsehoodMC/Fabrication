package com.unascribed.fabrication.mixin.f_balance.infinity_crossbows;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyGetField;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
@EligibleIf(anyConfigAvailable={"*.infinity_crossbows", "*.infinity_crossbows_modded"})
public class MixinPlayerEntity {

	@ModifyGetField(target="net/minecraft/entity/player/PlayerAbilities.creativeMode:Z",
			method="getArrowType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;")
	private static boolean fabrication$redirectIsCreativeMode(boolean old, PlayerAbilities subject, PlayerEntity pe, ItemStack crossbow) {
		if (FabConf.isAnyEnabled("*.infinity_crossbows") && FabConf.isEnabled("*.infibows")
				&& EnchantmentHelper.getLevel(Enchantments.INFINITY, crossbow) > 0) {
			return true;
		}
		return subject.creativeMode;
	}

}
