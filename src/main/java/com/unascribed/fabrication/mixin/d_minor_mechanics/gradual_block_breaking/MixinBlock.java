package com.unascribed.fabrication.mixin.d_minor_mechanics.gradual_block_breaking;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Optional;

@Mixin(Block.class)
@EligibleIf(configAvailable="*.gradual_block_breaking")
public abstract class MixinBlock {

	@ModifyArgs(at=@At(value="INVOKE", target="Lnet/minecraft/block/Block;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V"),
			method="afterBreak(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/item/ItemStack;)V")
	public void gradualBreak(Args args) {
		if (!FabConf.isEnabled("*.gradual_block_breaking")) return;
		Entity player = args.get(4);
		if (player == null || !ConfigPredicates.shouldRun("*.gradual_block_breaking", (PlayerEntity)player)) return;
		BlockState state = args.get(0);
		if (state.contains(SlabBlock.TYPE)) {
			if (state.get(SlabBlock.TYPE) == SlabType.DOUBLE) {
				World world = args.get(1);
				BlockPos pos = args.get(2);
				Box box = state.getCollisionShape(world, pos).getBoundingBox().offset(pos);
				Vec3d camPos = player.getCameraPosVec(1);
				Vec3d rot = player.getRotationVec(1);
				double d = player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) + 1;
				Vec3d vec3d3 = camPos.add(rot.x * d, rot.y * d, rot.z * d);
				Optional<Vec3d> optional = box.raycast(camPos, vec3d3);
				if (optional.isPresent()) {
					if (optional.get().y-pos.getY() < 0.5) {
						world.setBlockState(pos, state.with(SlabBlock.TYPE, SlabType.TOP));
						args.set(0, state.with(SlabBlock.TYPE, SlabType.BOTTOM));
					} else {
						world.setBlockState(pos, state.with(SlabBlock.TYPE, SlabType.BOTTOM));
						args.set(0, state.with(SlabBlock.TYPE, SlabType.TOP));
					}
				}
			}
		} else if (state.contains(SnowBlock.LAYERS)) {
			int layers = state.get(SnowBlock.LAYERS);
			if (layers > 1) {
				((World)args.get(1)).setBlockState(args.get(2), state.with(SnowBlock.LAYERS, layers-1));
				args.set(0, state.with(SnowBlock.LAYERS, 1));
			}
		}
	}

}
