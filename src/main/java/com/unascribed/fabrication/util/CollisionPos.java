package com.unascribed.fabrication.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.Optional;

public class CollisionPos {
	public static Optional<Vec3d> getClosestPointTo(VoxelShape self, Vec3d target) {
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
