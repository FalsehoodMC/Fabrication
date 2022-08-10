package com.unascribed.fabrication.mixin.a_fixes.stable_cacti;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.Material;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Iterator;

@Mixin(CactusBlock.class)
@EligibleIf(configAvailable="*.stable_cacti")
public class MixinCactusBlock extends Block {

	public MixinCactusBlock(Settings settings) {
		super(settings);
	}


	@FabModifyVariable(at=@At("STORE"),
			method="canPlaceAt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z")
	public Iterator<Direction> returnEmptyIter(Iterator<Direction> old) {
		if (FabConf.isAnyEnabled("*.stable_cacti")) return Collections.emptyIterator();
		return old;
	}

	@FabInject(at=@At("HEAD"), method="getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;")
	public void getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom, CallbackInfoReturnable<BlockState> ci) {
		if (!FabConf.isAnyEnabled("*.stable_cacti")) return;
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
			if (shouldBreak && !FabConf.isEnabled("*.stable_cacti_break_vanilla_compat")) {
				world.breakBlock(posFrom, true);
			}
		}
	}

}
