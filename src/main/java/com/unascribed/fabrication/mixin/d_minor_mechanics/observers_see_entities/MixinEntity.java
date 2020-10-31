package com.unascribed.fabrication.mixin.d_minor_mechanics.observers_see_entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
import net.minecraft.util.math.Direction;
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
	
	private final BlockPos.Mutable fabrication$observerpos1 = new BlockPos.Mutable();
	private final BlockPos.Mutable fabrication$observerpos2 = new BlockPos.Mutable();
	private final BlockPos.Mutable fabrication$observerpos3 = new BlockPos.Mutable();
	
	@Inject(at=@At("TAIL"), method="move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V")
	public void move(MovementType type, Vec3d movement, CallbackInfo ci) {
		if (!RuntimeChecks.check("*.observers_see_entities")) return;
		Object self = this;
		if ((self instanceof LivingEntity || !MixinConfigPlugin.isEnabled("*.observers_see_entities_living_only")) && !world.isClient && movement.lengthSquared() > 0.00615) {
			world.getProfiler().push("move");
			world.getProfiler().push("fabrication:observerCheck");
			Box box = getBoundingBox();
			fabrication$observerCheck(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, Direction.DOWN);
			fabrication$observerCheck(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, Direction.UP);
			
			fabrication$observerCheck(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ, Direction.WEST);
			fabrication$observerCheck(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, Direction.EAST);
			
			fabrication$observerCheck(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, Direction.NORTH);
			fabrication$observerCheck(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, Direction.SOUTH);
			world.getProfiler().pop();
			world.getProfiler().pop();
		}
	}
	
	private void fabrication$observerCheck(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Direction dir) {
		BlockPos min = fabrication$observerpos1.set(minX+dir.getOffsetX(), minY+dir.getOffsetY(), minZ+dir.getOffsetZ());
		BlockPos max = fabrication$observerpos2.set(maxX+dir.getOffsetX(), maxY+dir.getOffsetY(), maxZ+dir.getOffsetZ());
		BlockPos.Mutable mut = fabrication$observerpos3;
		if (world.isRegionLoaded(min, max)) {
			for (int x = min.getX(); x <= max.getX(); x++) {
				for (int y = min.getY(); y <= max.getY(); y++) {
					for (int z = min.getZ(); z <= max.getZ(); z++) {
						mut.set(x, y, z);
						BlockState bs = world.getBlockState(mut);
						if (bs.getBlock() == Blocks.OBSERVER && world.getBlockState(mut.offset(dir.getOpposite())).isAir()) {
							if (!bs.get(ObserverBlock.POWERED) && bs.get(ObserverBlock.FACING) == dir.getOpposite()) {
								bs.scheduledTick((ServerWorld)world, mut.toImmutable(), world.random);
							}
						}
					}
				}
			}
		}
	}
	
}
