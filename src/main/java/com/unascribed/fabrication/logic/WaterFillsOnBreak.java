package com.unascribed.fabrication.logic;

import com.google.common.collect.ImmutableSet;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabRefl;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class WaterFillsOnBreak {

	private static final ImmutableSet<Direction> CHECK_DIRECTIONS = ImmutableSet.of(
			Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
			);

	public static FlowableFluid shouldFill(World world, BlockPos pos) {
		int countWater = 0;
		int countAir = 0;
		FlowableFluid retFluid = null;
		BlockPos lastWater = null;
		for (Direction d : CHECK_DIRECTIONS) {
			BlockPos p = pos.offset(d);
			FluidState fluidState = world.getFluidState(p);
			Fluid fluid = fluidState.getFluid();

			if (fluidState.isStill()) {
				if (!(fluid instanceof FlowableFluid && FabRefl.isInfinate((FlowableFluid) fluid, world))) continue;
				if (retFluid == null) retFluid = (FlowableFluid) fluid;
				else if (!fluid.matchesType(retFluid)) continue;
				lastWater = p;
				countWater++;
			} else if (d != Direction.UP) {
				BlockState bs = world.getBlockState(p);
				if (bs.isAir()) {
					countAir++;
				}
			}
		}
		if (!(FabConf.isEnabled("*.water_fills_on_break_strict") && countWater == 1)){
			return countWater > countAir ? retFluid : null;
		}

		for (Direction d : Direction.values()) {
			FluidState fluidState = world.getFluidState(lastWater.offset(d));
			Fluid fluid = fluidState.getFluid();

			if (fluidState.isStill() && fluid.matchesType(retFluid)) {
				return retFluid;
			}
		}
		return null;
	}

}
