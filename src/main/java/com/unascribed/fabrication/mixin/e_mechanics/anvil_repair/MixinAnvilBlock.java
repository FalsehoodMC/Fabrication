package com.unascribed.fabrication.mixin.e_mechanics.anvil_repair;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(AnvilBlock.class)
@EligibleIf(configEnabled="*.anvil_repair")
public class MixinAnvilBlock {

	@Inject(at=@At("HEAD"), method="onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
			cancellable=true)
	public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {
		if (!MixinConfigPlugin.isEnabled("*.anvil_repair")) return;
		if (!world.isClient) {
			ItemStack held = player.getStackInHand(hand);
			if (held.getItem() == Item.fromBlock(Blocks.IRON_BLOCK)) {
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
					ci.setReturnValue(ActionResult.SUCCESS);
				}
			}
		}
	}
	
}
