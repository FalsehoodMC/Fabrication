package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.unascribed.fabrication.support.injection.Hijack;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireworkRocketEntity;

import java.util.Optional;

@Mixin(FireworkRocketEntity.class)
@EligibleIf(configAvailable="*.canhit")
public class MixinFireworkRocketEntity {

	@Hijack(target={"Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", "Lnet/minecraft/class_1309;method_5643(Lnet/minecraft/class_1282;F)Z"},
			method={"explode()V", "method_7475()V"})
	private static Optional<Boolean> fabrication$canDamage(LivingEntity subject, DamageSource source, float amount, FireworkRocketEntity self) {
		if (MixinConfigPlugin.isEnabled("*.canhit") && self instanceof SetCanHitList) {
			SetCanHitList schl = (SetCanHitList)self;
			if (!CanHitUtil.canHit(schl.fabrication$getCanHitList(), subject) || !CanHitUtil.canHit(schl.fabrication$getCanHitList2(), subject)) {
				return Optional.of(false);
			}
		}
		return Optional.empty();
	}

}
