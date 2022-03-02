package com.unascribed.fabrication.logic;

import com.google.common.collect.ImmutableSet;

import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class WaterFillsOnBreak {

	private static final ImmutableSet<Direction> CHECK_DIRECTIONS = ImmutableSet.of(
			Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
			);

	public static boolean shouldFill(World world, BlockPos pos) {
		int countWater = 0;
		int countAir = 0;
		BlockPos lastWater = null;
		for (Direction d : CHECK_DIRECTIONS) {
			BlockPos p = pos.offset(d);
			FluidState fluid = world.getFluidState(p);

			if (fluid.isIn(FluidTags.WATER) && fluid.isStill()) {
				lastWater = p;
				countWater++;
			} else if (d != Direction.UP) {
				BlockState bs = world.getBlockState(p);
				if (bs.isAir()) {
					countAir++;
				}
			}
		}
		if (!(MixinConfigPlugin.isEnabled("*.water_fills_on_break_strict") && countWater == 1)){
			return countWater > countAir;
		}

		for (Direction d : Direction.values()) {
			FluidState fluid = world.getFluidState(lastWater.offset(d));

			if (fluid.isIn(FluidTags.WATER) && fluid.isStill()) {
				return true;
			}
		}
		return false;
	}

}
