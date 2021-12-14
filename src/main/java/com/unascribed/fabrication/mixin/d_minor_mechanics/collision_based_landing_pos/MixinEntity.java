package com.unascribed.fabrication.mixin.d_minor_mechanics.collision_based_landing_pos;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.collision_based_landing_pos")
public abstract class MixinEntity {

	@Shadow public World world;
	@Shadow private Box entityBounds;

	@Shadow public abstract double getY();
	@Shadow public abstract double getZ();
	@Shadow public abstract Vec3d getPos();

	@Inject(method="getLandingPos()Lnet/minecraft/util/math/BlockPos;", at=@At(value="HEAD"), cancellable=true)
	public void getLandingPos(CallbackInfoReturnable<BlockPos> cir) {
		if (!MixinConfigPlugin.isEnabled("*.collision_based_landing_pos")) return;
		world.getBlockCollisions((Entity)(Object)this, this.entityBounds.offset(0, -0.20000000298023224D, 0), (state, pos) -> pos.getY() <= this.getY()).findFirst().ifPresent(
				vs -> cir.setReturnValue(new BlockPos(vs.getBoundingBox().getCenter()))
		);
	}

}
