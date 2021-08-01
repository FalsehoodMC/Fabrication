package com.unascribed.fabrication.mixin.z_combined.furnace_minecart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.unascribed.fabrication.interfaces.WasShoved;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(anyConfigAvailable={"*.hyperspeed_furnace_minecart", "*.furnace_minecart_pushing"})
public abstract class MixinFurnaceMinecartEntity {

	@ModifyArgs(at=@At(value="INVOKE", target="net/minecraft/util/math/Vec3d.add(DDD)Lnet/minecraft/util/math/Vec3d;"),
			method="applySlowdown()V")
	public void modifyApplySlowdownVelocity(Args args) {
		double speed = 1;
		boolean shoved = (this instanceof WasShoved && ((WasShoved)this).fabrication$wasShoved());
		if (shoved) {
			speed = 0.2;
		} else if (MixinConfigPlugin.isEnabled("*.hyperspeed_furnace_minecart") && !shoved) {
			speed = 2;
		}
		if (speed != 1) {
			args.set(0, ((double)args.get(0))*speed);
			args.set(1, ((double)args.get(1))*speed);
			args.set(2, ((double)args.get(2))*speed);
		}
	}
	
}
