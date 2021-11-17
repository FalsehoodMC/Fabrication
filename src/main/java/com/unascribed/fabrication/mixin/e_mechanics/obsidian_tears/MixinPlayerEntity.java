package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.obsidian_tears")
public class MixinPlayerEntity {

	@Inject(at=@At("HEAD"), method="findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;",
			cancellable=true)
	private static void findRespawnPosition(ServerWorld world, BlockPos pos, float f, boolean b, boolean b2, CallbackInfoReturnable<Optional<Vec3d>> ci) {
		if (!MixinConfigPlugin.isEnabled("*.obsidian_tears")) return;
		BlockState state = world.getBlockState(pos);
		Block bl = state.getBlock();
		if (bl == Blocks.CRYING_OBSIDIAN) {
			if (world.getBlockState(pos.up()).getBlock().canMobSpawnInside() && world.getBlockState(pos.up().up()).getBlock().canMobSpawnInside()) {
				ci.setReturnValue(Optional.of(new Vec3d(pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5)));
			} else {
				Optional<Vec3d> attempt = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, pos);
				if (attempt.isPresent()) {
					ci.setReturnValue(attempt);
				}
			}
		}
	}

}
