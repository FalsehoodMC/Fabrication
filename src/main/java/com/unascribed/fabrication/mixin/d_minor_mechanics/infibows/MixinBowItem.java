package com.unascribed.fabrication.mixin.d_minor_mechanics.infibows;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(BowItem.class)
@EligibleIf(configAvailable="*.infibows", classNotPresent="net.parker8283.bif.BowInfinityFix")
public class MixinBowItem {

	@ModifyVariable(at=@At("STORE"), method="use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;")
	private boolean infiBow(boolean hasArrows, World world, PlayerEntity player, Hand hand) {
		if (MixinConfigPlugin.isEnabled("*.infibows") && EnchantmentHelper.getLevel(Enchantments.INFINITY, player.getStackInHand(hand)) > 0)
			return true;
		return hasArrows;
	}

}
