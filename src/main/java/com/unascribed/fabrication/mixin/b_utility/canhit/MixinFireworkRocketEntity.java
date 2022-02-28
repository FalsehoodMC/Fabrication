package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.FireworkRocketEntity;

@Mixin(FireworkRocketEntity.class)
@EligibleIf(configAvailable="*.canhit")
public class MixinFireworkRocketEntity {

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/entity/LivingEntity.damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
			method="explode()V")
	public boolean damage(LivingEntity subject, DamageSource source, float amt) {
		if (FabConf.isEnabled("*.canhit") && this instanceof SetCanHitList) {
			SetCanHitList schl = (SetCanHitList)this;
			if (!CanHitUtil.canHit(schl.fabrication$getCanHitList(), subject) || !CanHitUtil.canHit(schl.fabrication$getCanHitList2(), subject)) {
				return false;
			}
		}
		return subject.damage(source, amt);
	}

}
