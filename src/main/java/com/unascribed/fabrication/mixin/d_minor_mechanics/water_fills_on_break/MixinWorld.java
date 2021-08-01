package com.unascribed.fabrication.mixin.d_minor_mechanics.water_fills_on_break;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.google.common.collect.ImmutableSet;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(World.class)
@EligibleIf(configAvailable="*.water_fills_on_break")
public class MixinWorld {

	private static final ImmutableSet<Direction> FABRICATION$CHECKDIRECTIONS = ImmutableSet.of(
			Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
		);
	
	@Inject(at=@At("HEAD"), method="removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z", cancellable=true)
	public void removeBlock(BlockPos pos, boolean move, CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.water_fills_on_break")) {
			World self = (World)(Object)this;
			int countWater = 0;
			int countAir = 0;
			for (Direction d : FABRICATION$CHECKDIRECTIONS) {
				BlockPos p = pos.offset(d);
				FluidState fluid = self.getFluidState(p);
				
				if (fluid.isIn(FluidTags.WATER) && fluid.isStill()) {
					countWater++;
				} else if (d != Direction.UP) {
					BlockState bs = self.getBlockState(p);
					if (bs.isAir()) {
						countAir++;
					}
				}
			}
			if (countWater > countAir) {
				ci.setReturnValue(self.setBlockState(pos, Fluids.WATER.getDefaultState().getBlockState(), 3 | (move ? 64 : 0)));
			}
		}
	}
	
}
