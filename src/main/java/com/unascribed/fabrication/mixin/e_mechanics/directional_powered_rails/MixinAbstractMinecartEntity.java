package com.unascribed.fabrication.mixin.e_mechanics.directional_powered_rails;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(AbstractMinecartEntity.class)
@EligibleIf(configAvailable="*.directional_powered_rails")
public abstract class MixinAbstractMinecartEntity extends Entity {

	public MixinAbstractMinecartEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@FabInject(at=@At("TAIL"), method="moveOnRail(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
	protected void moveOnRail(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.directional_powered_rails")) return;
		if (state.isOf(Blocks.POWERED_RAIL)) {
			BlockPos down = pos.down();
			BlockState downState = getWorld().getBlockState(down);
			if (downState.isOf(Blocks.MAGENTA_GLAZED_TERRACOTTA)) {
				Direction dir = downState.get(GlazedTerracottaBlock.FACING).getOpposite();
				Vec3d vel = getVelocity();
				if (state.get(PoweredRailBlock.POWERED)) {
					double addition = 0.1;
					if (dir.getAxis() == Axis.X) {
						if (dir.getDirection().offset() != Math.signum(vel.getX())) {
							vel = vel.multiply(0, 1, 1);
						}
					}
					if (dir.getAxis() == Axis.Z) {
						if (dir.getDirection().offset() != Math.signum(vel.getZ())) {
							vel = vel.multiply(1, 1, 0);
						}
					}
					vel = vel.add(dir.getOffsetX()*addition, dir.getOffsetY()*addition, dir.getOffsetZ()*addition);
				} else {
					vel = vel.multiply(0.025);
				}
				setVelocity(vel);
			}
		}
	}


}
