package com.unascribed.fabrication.mixin.g_weird_tweaks.underwater_explosions;

import java.util.Optional;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

@Mixin(ExplosionBehavior.class)
@EligibleIf(configAvailable="*.underwater_explosions")
public abstract class MixinExplosionBehavior {

	@FabInject(at=@At(value="HEAD"), method="getBlastResistance(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Ljava/util/Optional;", cancellable=true)
	public void getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, CallbackInfoReturnable<Optional<Float>> ci) {
		if (FabConf.isEnabled("*.underwater_explosions") && !fluidState.isEmpty()) {
			ci.setReturnValue(Optional.empty());
		}
	}

}
