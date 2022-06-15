package com.unascribed.fabrication.mixin.c_tweaks.nether_cauldron;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(AbstractCauldronBlock.class)
@EligibleIf(configAvailable="*.nether_cauldron")
public class MixinAbstractCauldronBlock {
	@Inject(at=@At("HEAD"), method= "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", cancellable = true)
	private void setLevel(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
		if (FabConf.isEnabled("*.nether_cauldron") && player.getStackInHand(hand).getItem().equals(Items.WATER_BUCKET) && world.getDimension().ultrawarm()) {
			world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
			if (world instanceof ServerWorld)
				((ServerWorld)world).spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX()+0.5, pos.getY()+0.6, pos.getZ()+0.5, 8, 0.2, 0.2, 0.2, 0);
			cir.setReturnValue(ActionResult.CONSUME);
		}
	}
}
