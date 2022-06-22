package com.unascribed.fabrication.mixin.d_minor_mechanics.water_fills_on_break;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.logic.WaterFillsOnBreak;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;

import net.minecraft.fluid.Fluids;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(ServerPlayerInteractionManager.class)
@EligibleIf(anyConfigAvailable={"*.water_fills_on_break", "*.water_fills_on_break_strict"}, specialConditions=SpecialEligibility.FORGE)
public class MixinServerPlayerInteractionManager {

	@Shadow
	protected ServerWorld world;

	// Forge replaces the call to World.removeBlock with one into IForgeBlockState.removedByPlayer
	// I can't mixin to a default interface method, so this will have to do

	@FabInject(at=@At("RETURN"), method="tryBreakBlock(Lnet/minecraft/util/math/BlockPos;)Z")
	public void tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isAnyEnabled("*.water_fills_on_break") && ci.getReturnValueZ()) {
			if (WaterFillsOnBreak.shouldFill(world, pos) && world.getBlockState(pos).isAir()) {
				world.setBlockState(pos, Fluids.WATER.getDefaultState().getBlockState());
			}
		}
	}


}
