package com.unascribed.fabrication.mixin.a_fixes.stable_cacti;

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.Material;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

@Mixin(CactusBlock.class)
@EligibleIf(configAvailable="*.stable_cacti")
public class MixinCactusBlock extends Block {

	public MixinCactusBlock(Settings settings) {
		super(settings);
	}


	@Redirect(at=@At(value="INVOKE", target="net/minecraft/util/math/Direction$Type.iterator()Ljava/util/Iterator;"),
			method="canPlaceAt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z")
	public Iterator<Direction> getHorizontalDirectionIterator(Direction.Type subject) {
		if (MixinConfigPlugin.isEnabled("*.stable_cacti")) return Collections.emptyIterator();
		return subject.iterator();
	}

	@Inject(at=@At("HEAD"), method="getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;")
	public void getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom, CallbackInfoReturnable<BlockState> ci) {
		if (!MixinConfigPlugin.isEnabled("*.stable_cacti")) return;
		if (direction == Direction.UP &&
				state.getBlock() == this && state.get(CactusBlock.AGE) > 0 &&
				newState.getBlock() == this && newState.get(CactusBlock.AGE) == 0) {
			// we just grew
			boolean shouldBreak = false;
			for (Direction d : Direction.Type.HORIZONTAL) {
				BlockPos p = posFrom.offset(d);
				BlockState bs = world.getBlockState(p);
				Material m = bs.getMaterial();
				if (bs.getBlock() != this && (m.isSolid() || world.getFluidState(p).isIn(FluidTags.LAVA))) {
					shouldBreak = true;
					break;
				}
			}
			if (shouldBreak) {
				world.getBlockTickScheduler().schedule(posFrom, this, 1);
			}
		}
	}

	@Inject(at=@At("HEAD"), method="scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V", cancellable=true)
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.stable_cacti")) return;
		world.breakBlock(pos, true);
		ci.cancel();
	}

}
