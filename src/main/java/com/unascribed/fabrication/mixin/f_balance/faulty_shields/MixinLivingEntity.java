package com.unascribed.fabrication.mixin.f_balance.faulty_shields;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.faulty_shields")
public abstract class MixinLivingEntity {

	private float fabrication$shieldUnblockedDamage = 0f;

	@FabInject(method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
			at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
	public void preShieldDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.faulty_shields") || source.isIn(DamageTypeTags.IS_PROJECTILE)) return;
		fabrication$shieldUnblockedDamage = amount/2f;
	}

	@FabModifyVariable(method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", argsOnly=true,
			at=@At(value="FIELD", target="Lnet/minecraft/registry/tag/DamageTypeTags;IS_PROJECTILE:Lnet/minecraft/registry/tag/TagKey;"))
	public float postShieldDamage(float amount) {
		if (fabrication$shieldUnblockedDamage == 0f) return amount;
		float ret = fabrication$shieldUnblockedDamage;
		fabrication$shieldUnblockedDamage = 0f;
		return ret;
	}
}
