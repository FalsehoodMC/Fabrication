package com.unascribed.fabrication.mixin.d_minor_mechanics.gradual_block_breaking;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.GradualBreak;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ServerPlayerInteractionManager.class)
@EligibleIf(configAvailable="*.gradual_block_breaking")
public class MixinServerPlayerInteractionManager implements GradualBreak {

	@Shadow
	@Final
	protected ServerPlayerEntity player;

	@Shadow
	protected ServerWorld world;

	private BlockState fabrication$gradualBreakState = null;

	@ModifyReturn(target="Lnet/minecraft/server/world/ServerWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
			method="tryBreakBlock(Lnet/minecraft/util/math/BlockPos;)Z")
	private static BlockState fabrication$gradualBreak(BlockState state, ServerWorld world, BlockPos pos, ServerPlayerInteractionManager self) {
		if (!(FabConf.isEnabled("*.gradual_block_breaking") && self instanceof AccessorServerPlayerInteractionManager && self instanceof GradualBreak)) return state;
		ServerPlayerEntity player = ((AccessorServerPlayerInteractionManager)self).fabrication$getPlayer();
		if (player == null || !ConfigPredicates.shouldRun("*.gradual_block_breaking", (PlayerEntity)player)) return state;
		if (state.contains(SlabBlock.TYPE)) {
			if (state.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
				Box box = state.getCollisionShape(world, pos).getBoundingBox().offset(pos);
				Vec3d camPos = player.getCameraPosVec(1);
				Vec3d rot = player.getRotationVec(1);
				double d = player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) + 1;
				Vec3d vec3d3 = camPos.add(rot.x * d, rot.y * d, rot.z * d);
				Optional<Vec3d> optional = box.raycast(camPos, vec3d3);
				if (optional.isPresent()) {
					if (optional.get().y-pos.getY() < 0.5) {
						((GradualBreak)self).fabrication$setGradualBreak(state.with(SlabBlock.TYPE, SlabType.TOP));
						return state.with(SlabBlock.TYPE, SlabType.BOTTOM);
					} else {
						((GradualBreak)self).fabrication$setGradualBreak(state.with(SlabBlock.TYPE, SlabType.BOTTOM));
						return  state.with(SlabBlock.TYPE, SlabType.TOP);
					}
				}
			}
		} else if (state.contains(SnowBlock.LAYERS)) {
			int layers = state.get(SnowBlock.LAYERS);
			if (layers > 1) {
				((GradualBreak)self).fabrication$setGradualBreak(state.with(SnowBlock.LAYERS, layers-1));
				return state.with(SnowBlock.LAYERS, 1);
			}
		}
		return state;
	}

	@Hijack(target="Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z",
			method="tryBreakBlock(Lnet/minecraft/util/math/BlockPos;)Z")
	private static HijackReturn fabrication$gradualBreak(ServerWorld world, BlockPos pos, boolean move, ServerPlayerInteractionManager self) {
		if (!(FabConf.isEnabled("*.gradual_block_breaking") && self instanceof GradualBreak)) return HijackReturn.empty;
		BlockState state = ((GradualBreak)self).fabrication$getGradualBreak();
		if (state != null) {
			world.setBlockState(pos, state);
			((GradualBreak)self).fabrication$setGradualBreak(null);
			return new HijackReturn(true);
		}
		return HijackReturn.empty;
	}

	@Override
	public void fabrication$setGradualBreak(BlockState state) {
		fabrication$gradualBreakState = state;
	}

	@Override
	public BlockState fabrication$getGradualBreak() {
		return fabrication$gradualBreakState;
	}

}
