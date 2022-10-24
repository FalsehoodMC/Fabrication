package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.HijackReturn;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireworkRocketEntity;

@Mixin(FireworkRocketEntity.class)
@EligibleIf(configAvailable="*.canhit")
public class MixinFireworkRocketEntity {

	@Hijack(target="Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", method="explode()V")
	private static HijackReturn fabrication$canDamage(LivingEntity subject, DamageSource source, float amount, FireworkRocketEntity self) {
		if (FabConf.isEnabled("*.canhit") && self instanceof SetCanHitList) {
			SetCanHitList schl = (SetCanHitList)self;
			if (!CanHitUtil.canHit(schl.fabrication$getCanHitList(), subject) || !CanHitUtil.canHit(schl.fabrication$getCanHitList2(), subject)) {
				return HijackReturn.FALSE;
			}
		}
		return null;
	}

}
