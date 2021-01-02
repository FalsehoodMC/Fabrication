package com.unascribed.fabrication.mixin.d_minor_mechanics.observers_see_entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
@EligibleIf(configEnabled="*.observers_see_entities")
public abstract class MixinEntity {

	@Shadow
	public Vec3d pos;
	@Shadow
	public World world;
	
	@Shadow
	public abstract BlockPos getBlockPos();
	@Shadow
	public abstract Box getBoundingBox();
	
	@Inject(at=@At("TAIL"), method="move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V")
	public void move(MovementType type, Vec3d movement, CallbackInfo ci) {
		if (!RuntimeChecks.check("*.observers_see_entities")) return;
		Object self = this;
		if ((self instanceof LivingEntity || !MixinConfigPlugin.isEnabled("*.observers_see_entities_living_only")) && !world.isClient && movement.lengthSquared() > 0.00615) {
			world.getProfiler().push("move");
			world.getProfiler().push("fabrication:observerCheck");
			FabricationMod.forAllAdjacentBlocks((Entity)self, (w, bp, bp2, dir) -> {
				BlockState bs = world.getBlockState(bp);
				if (bs.getBlock() == Blocks.OBSERVER && world.getBlockState(bp2.offset(dir.getOpposite())).isAir()) {
					if (!bs.get(ObserverBlock.POWERED) && bs.get(ObserverBlock.FACING) == dir.getOpposite()) {
						bs.scheduledTick((ServerWorld)world, bp.toImmutable(), world.random);
					}
				}
				return true;
			});
			world.getProfiler().pop();
			world.getProfiler().pop();
		}
	}
	
}
