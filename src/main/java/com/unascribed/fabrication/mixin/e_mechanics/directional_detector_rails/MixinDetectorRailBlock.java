package com.unascribed.fabrication.mixin.e_mechanics.directional_detector_rails;

import java.util.function.Predicate;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(DetectorRailBlock.class)
@EligibleIf(configAvailable="*.directional_detector_rails")
public class MixinDetectorRailBlock {

	@FabModifyArg(at=@At(value="INVOKE", target="net/minecraft/block/DetectorRailBlock.getCarts(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/lang/Class;Ljava/util/function/Predicate;)Ljava/util/List;"),
			method="updatePoweredStatus(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", index=3)
	private Predicate<Entity> changePredicate(World world, BlockPos pos, Class<?> type, Predicate<Entity> orig) {
		if (!FabConf.isEnabled("*.directional_detector_rails")) return orig;
		BlockPos down = pos.down();
		BlockState downState = world.getBlockState(down);
		if (downState.isOf(Blocks.MAGENTA_GLAZED_TERRACOTTA)) {
			Direction dir = downState.get(GlazedTerracottaBlock.FACING).getOpposite();
			return e -> Math.signum(e.getVelocity().getComponentAlongAxis(dir.getAxis())) == dir.getDirection().offset();
		}
		return orig;
	}

}
