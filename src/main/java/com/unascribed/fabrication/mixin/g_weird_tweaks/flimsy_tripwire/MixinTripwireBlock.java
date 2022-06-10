package com.unascribed.fabrication.mixin.g_weird_tweaks.flimsy_tripwire;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import net.minecraft.block.TripwireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TripwireBlock.class)
@EligibleIf(configAvailable="*.flimsy_tripwire")
public abstract class MixinTripwireBlock {

	@Hijack(target="Lnet/minecraft/block/TripwireBlock;updatePowered(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
			method="scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V")
	private static boolean fabrication$breakWire(TripwireBlock self, ServerWorld world, BlockPos pos){
		if (!(FabConf.isEnabled("*.flimsy_tripwire") && world.getBlockState(pos).get(TripwireBlock.ATTACHED))) return false;
		world.breakBlock(pos, true);
		return true;
	}

}
