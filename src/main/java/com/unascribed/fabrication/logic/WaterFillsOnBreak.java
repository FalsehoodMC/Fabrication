package com.unascribed.fabrication.logic;

import com.google.common.collect.ImmutableSet;

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
		for (Direction d : CHECK_DIRECTIONS) {
			BlockPos p = pos.offset(d);
			FluidState fluid = world.getFluidState(p);

			if (fluid.isIn(FluidTags.WATER) && fluid.isStill()) {
				countWater++;
			} else if (d != Direction.UP) {
				BlockState bs = world.getBlockState(p);
				if (bs.isAir()) {
					countAir++;
				}
			}
		}
		return countWater > countAir;
	}

}
