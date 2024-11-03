package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.logic.ObsidianTears;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

@Mixin(AbstractBlock.AbstractBlockState.class)
@EligibleIf(configAvailable="*.obsidian_tears")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public class MixinAbstractBlockState {

	@FabInject(at=@At("HEAD"), method="onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;",
			cancellable=true)
	public void onUse(ItemStack held, World world, PlayerEntity user, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ItemActionResult> ci) {
		if (FabConf.isEnabled("*.obsidian_tears") && world.getBlockState(hit.getBlockPos()).getBlock() == Blocks.CRYING_OBSIDIAN) {
			if (held.getItem() == Items.GLASS_BOTTLE) {
				world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1, 1);
				if (user != null) {
					user.incrementStat(Stats.USED.getOrCreateStat(Items.GLASS_BOTTLE));
				}
				ItemStack stack = ObsidianTears.createStack(world, hit.getBlockPos());
				user.setStackInHand(hand, ItemUsage.exchangeStack(held, user, stack));
				ci.setReturnValue(ItemActionResult.SUCCESS);
			}
		}
	}

}
