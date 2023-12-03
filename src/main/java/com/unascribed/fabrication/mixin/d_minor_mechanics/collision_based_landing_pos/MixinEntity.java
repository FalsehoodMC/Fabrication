package com.unascribed.fabrication.mixin.d_minor_mechanics.collision_based_landing_pos;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.collision_based_landing_pos")
public abstract class MixinEntity {

	private static final Predicate<Entity> fabrication$collisionBasedLandingPos = ConfigPredicates.getFinalPredicate("*.collision_based_landing_pos");

	@FabInject(method="getLandingPos()Lnet/minecraft/util/math/BlockPos;", at=@At(value="RETURN"), cancellable=true)
	public void getLandingPos(CallbackInfoReturnable<BlockPos> cir) {
		if (!FabConf.isEnabled("*.collision_based_landing_pos")) return;
		Entity self = (Entity)(Object)this;
		World world = self.world;
		if (!fabrication$collisionBasedLandingPos.test(self)) return;
		VoxelShape inp = world.getBlockState(cir.getReturnValue()).getCollisionShape(world, cir.getReturnValue());
		if (!inp.isEmpty()) return;
		Box boundingBox = self.getBoundingBox();
		Vec3d pos = self.getPos();
		Optional<VoxelShape> ret = StreamSupport.stream(world.getBlockCollisions(self, new Box(boundingBox.minX, boundingBox.minY - 0.20000000298023224D, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)).spliterator(), false).min(Comparator.comparing(a -> fabrication$vs$getClosestPointTo(a, pos).map(vec3d -> vec3d.distanceTo(pos)).orElse(Double.POSITIVE_INFINITY)));
		if (ret.isPresent()) {
			VoxelShape shape = ret.get();
			cir.setReturnValue(new BlockPos(
				shape.getMin(Direction.Axis.X),
				shape.getMin(Direction.Axis.Y),
				shape.getMin(Direction.Axis.Z)
			));
		}
	}
	private static Optional<Vec3d> fabrication$vs$getClosestPointTo(VoxelShape self, Vec3d target) {
		if (self.isEmpty()) {
			return Optional.empty();
		} else {
			Vec3d[] vec3ds = new Vec3d[1];
			self.forEachBox((d, e, f, g, h, i) -> {
				double j = MathHelper.clamp(target.getX(), d, g);
				double k = MathHelper.clamp(target.getY(), e, h);
				double l = MathHelper.clamp(target.getZ(), f, i);
				if (vec3ds[0] == null || target.squaredDistanceTo(j, k, l) < target.squaredDistanceTo(vec3ds[0])) {
					vec3ds[0] = new Vec3d(j, k, l);
				}

			});
			return Optional.of(vec3ds[0]);
		}
	}

}
