package com.unascribed.fabrication.mixin.f_balance.velocity_based_fall_damage_reset;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;


@Mixin(Entity.class)
@EligibleIf(configAvailable="*.velocity_based_fall_damage_reset")
public abstract class MixinLivingEntity {

	@Shadow
	public float fallDistance;

	@Shadow
	public abstract void onLanding();

	@Shadow
	private Vec3d velocity;

	private static final Predicate<Entity> fabrication$FallDamageResetPredicate = ConfigPredicates.getFinalPredicate("*.velocity_based_fall_damage_reset");

	@Hijack(method={"move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", "checkWaterState()V"}, target="Lnet/minecraft/entity/Entity;onLanding()V")
	private boolean fabrication$altFallReset() {
		if (!FabConf.isEnabled("*.velocity_based_fall_damage_reset")) return false;
		if (fabrication$FallDamageResetPredicate.test((Entity)(Object)this)) {
			if (this.velocity.y > 0.2) return false;
			this.fallDistance *= Math.max(-this.velocity.y, 5)/5.9;
			if (this.fallDistance < 1.1) {
				this.fallDistance = 0f;
				this.onLanding();
			}
			return true;
		}
		return false;
	}
}
