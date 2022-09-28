package com.unascribed.fabrication.mixin.z_combined.furnace_minecart;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.unascribed.fabrication.interfaces.WasShoved;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.vehicle.FurnaceMinecartEntity;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(anyConfigAvailable={"*.hyperspeed_furnace_minecart", "*.furnace_minecart_pushing"})
public abstract class MixinFurnaceMinecartEntity {

	@FabModifyArg(at=@At(value="INVOKE", target="net/minecraft/util/math/Vec3d.add(DDD)Lnet/minecraft/util/math/Vec3d;"),
			method="applySlowdown()V", index=0)
	public double modifyApplySlowdownVelocity0(double d) {
		return fabrication$modifyApplySlowdownVelocity(this, d);
	}
	@FabModifyArg(at=@At(value="INVOKE", target="net/minecraft/util/math/Vec3d.add(DDD)Lnet/minecraft/util/math/Vec3d;"),
			method="applySlowdown()V", index=1)
	public double modifyApplySlowdownVelocity1(double d) {
		return fabrication$modifyApplySlowdownVelocity(this, d);
	}
	@FabModifyArg(at=@At(value="INVOKE", target="net/minecraft/util/math/Vec3d.add(DDD)Lnet/minecraft/util/math/Vec3d;"),
			method="applySlowdown()V", index=2)
	public double modifyApplySlowdownVelocity2(double d) {
		return fabrication$modifyApplySlowdownVelocity(this, d);
	}
	private static double fabrication$modifyApplySlowdownVelocity(Object self, double args) {
		double speed = 1;
		if (self instanceof WasShoved && ((WasShoved)self).fabrication$wasShoved()) {
			speed = 0.2;
		} else if (FabConf.isEnabled("*.hyperspeed_furnace_minecart")) {
			speed = 2;
		}
		if (speed != 1) return args*speed;
		return args;
	}
}
