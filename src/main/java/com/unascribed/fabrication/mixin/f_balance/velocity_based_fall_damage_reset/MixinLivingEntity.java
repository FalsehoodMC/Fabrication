package com.unascribed.fabrication.mixin.f_balance.velocity_based_fall_damage_reset;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;


@Mixin(Entity.class)
@EligibleIf(configAvailable="*.velocity_based_fall_damage_reset")
public abstract class MixinLivingEntity {

	@Shadow
	public float fallDistance;
	@Shadow
	private Vec3d velocity;

	private float fabrication$lastFallDistanceVelFallDamage = 0f;
	private static final Predicate<Entity> fabrication$FallDamageResetPredicate = ConfigPredicates.getFinalPredicate("*.velocity_based_fall_damage_reset");

	@Inject(method="checkWaterState()V", at=@At("HEAD"))
	private void fabrication$memFallDistance(CallbackInfo ci) {
		if (FabConf.isEnabled("*.velocity_based_fall_damage_reset") && fabrication$FallDamageResetPredicate.test((Entity)(Object)this)) {
			fabrication$lastFallDistanceVelFallDamage = fallDistance;
		} else {
			fabrication$lastFallDistanceVelFallDamage = 0f;
		}
	}

	@Inject(method="checkWaterState()V", at=@At(value="FIELD", target="Lnet/minecraft/entity/Entity;fallDistance:F", shift=At.Shift.AFTER))
	private void fabrication$altFallReset(CallbackInfo ci) {
		if (fabrication$lastFallDistanceVelFallDamage == 0f) return;
		if (this.velocity.y > 0.2) return;
		this.fallDistance = fabrication$lastFallDistanceVelFallDamage;
		this.fallDistance *= Math.max(-this.velocity.y, 5)/5.9;
		if (this.fallDistance < 1.1) {
			this.fallDistance = 0f;
		}
	}
}
