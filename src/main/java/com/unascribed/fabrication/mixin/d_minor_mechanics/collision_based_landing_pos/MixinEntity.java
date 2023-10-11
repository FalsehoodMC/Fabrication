package com.unascribed.fabrication.mixin.d_minor_mechanics.collision_based_landing_pos;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.collision_based_landing_pos")
public abstract class MixinEntity {

	@Shadow public World world;
	@Shadow private Box entityBounds;

	@Shadow public abstract double getY();
	@Shadow public abstract double getZ();
	@Shadow public abstract Vec3d getPos();

	private static final Predicate<Entity> fabrication$collisionBasedLandingPos = ConfigPredicates.getFinalPredicate("*.collision_based_landing_pos");

	@FabInject(method="getLandingPos()Lnet/minecraft/util/math/BlockPos;", at=@At(value="HEAD"), cancellable=true)
	public void getLandingPos(CallbackInfoReturnable<BlockPos> cir) {
		if (!FabConf.isEnabled("*.collision_based_landing_pos")) return;
		if (!fabrication$collisionBasedLandingPos.test((Entity)(Object)this)) return;
		world.getBlockCollisions((Entity)(Object)this, this.entityBounds.offset(0, -0.20000000298023224D, 0), (state, pos) -> pos.getY() <= this.getY()).findFirst().ifPresent(
				vs -> cir.setReturnValue(new BlockPos(vs.getBoundingBox().getCenter()))
		);
	}

}
