package com.unascribed.fabrication.mixin.d_minor_mechanics.water_fills_on_break;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.logic.WaterFillsOnBreak;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(World.class)
@EligibleIf(configAvailable="*.water_fills_on_break")
public class MixinWorld {

	@Inject(at=@At("HEAD"), method="removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z", cancellable=true)
	public void removeBlock(BlockPos pos, boolean move, CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.water_fills_on_break")) {
			World self = (World)(Object)this;
			if (WaterFillsOnBreak.shouldFill(self, pos)) {
				ci.setReturnValue(self.setBlockState(pos, Fluids.WATER.getDefaultState().getBlockState(), 3 | (move ? 64 : 0)));
			}
		}
	}

	@Inject(at=@At("RETURN"), method="breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;I)Z", cancellable=true)
	public void breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth, CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.water_fills_on_break") && ci.getReturnValueZ()) {
			World self = (World)(Object)this;
			if (WaterFillsOnBreak.shouldFill(self, pos)) {
				ci.setReturnValue(self.setBlockState(pos, Fluids.WATER.getDefaultState().getBlockState(), 3));
			}
		}
	}
	
	
}
