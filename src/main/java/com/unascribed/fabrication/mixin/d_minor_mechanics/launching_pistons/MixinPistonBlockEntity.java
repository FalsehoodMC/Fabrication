package com.unascribed.fabrication.mixin.d_minor_mechanics.launching_pistons;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.PistonBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PistonBlockEntity.class)
@EligibleIf(configAvailable="*.launching_pistons")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public class MixinPistonBlockEntity {

	@ModifyReturn(target="Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z",
			method="pushEntities(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;FLnet/minecraft/block/entity/PistonBlockEntity;)V")
	private static boolean fabrication$launchPlayer(boolean old, BlockState state, Block block) {
		return old || FabConf.isEnabled("*.launching_pistons") && block == Blocks.SLIME_BLOCK;
	}

}
