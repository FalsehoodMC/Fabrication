package com.unascribed.fabrication.mixin.g_weird_tweaks.chaining_creepers;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.chaining_creepers")
public abstract class MixinLivingEntity {

	@Inject(method= "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at=@At("HEAD"), cancellable=true)
	public void lightCreepersOnExplosion(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		Object self = this;
		if (!(MixinConfigPlugin.isEnabled("*.chaining_creepers") && self instanceof CreeperEntity && source.isExplosive())) return;
		((CreeperEntity)self).ignite();
		cir.setReturnValue(false);
	}
}