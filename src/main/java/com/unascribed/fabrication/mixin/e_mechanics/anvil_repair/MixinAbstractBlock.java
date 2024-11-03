package com.unascribed.fabrication.mixin.e_mechanics.anvil_repair;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(AbstractBlock.class)
@EligibleIf(configAvailable="*.anvil_repair")
public class MixinAbstractBlock {

	@FabInject(at=@At("HEAD"), method="onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;",
			cancellable=true)
	public void onUse(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ItemActionResult> cir) {
		if (!((Object) this instanceof AnvilBlock)) return;
		if (!FabConf.isEnabled("*.anvil_repair")) return;
		if (!world.isClient) {
			ItemStack held = player.getStackInHand(hand);
			if (held.isOf(Blocks.IRON_BLOCK.asItem())) {
				BlockState bs = world.getBlockState(pos);
				boolean consume = false;
				if (bs.getBlock() == Blocks.DAMAGED_ANVIL) {
					world.setBlockState(pos, Blocks.CHIPPED_ANVIL.getDefaultState().with(AnvilBlock.FACING, bs.get(AnvilBlock.FACING)));
					consume = true;
				} else if (bs.getBlock() == Blocks.CHIPPED_ANVIL) {
					world.setBlockState(pos, Blocks.ANVIL.getDefaultState().with(AnvilBlock.FACING, bs.get(AnvilBlock.FACING)));
					consume = true;
				}
				if (consume) {
					world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1, 1);
					if (!player.getAbilities().creativeMode) {
						held.decrement(1);
					}
					cir.setReturnValue(ItemActionResult.SUCCESS);
				}
			}
		}
	}

}
