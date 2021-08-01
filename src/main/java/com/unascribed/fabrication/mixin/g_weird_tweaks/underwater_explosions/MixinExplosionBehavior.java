package com.unascribed.fabrication.mixin.g_weird_tweaks.underwater_explosions;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ExplosionBehavior.class)
@EligibleIf(configAvailable="*.underwater_explosions")
public abstract class MixinExplosionBehavior {

	@Inject(at=@At(value="HEAD"), method="getBlastResistance(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Ljava/util/Optional;", cancellable=true)
	public void getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, CallbackInfoReturnable<Optional<Float>> ci) {
		if (MixinConfigPlugin.isEnabled("*.underwater_explosions") && !fluidState.isEmpty()) {
			ci.setReturnValue(Optional.empty());
		}
	}

}
