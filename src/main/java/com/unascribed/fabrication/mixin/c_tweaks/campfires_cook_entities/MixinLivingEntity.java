package com.unascribed.fabrication.mixin.c_tweaks.campfires_cook_entities;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.campfires_cook_entities")
public class MixinLivingEntity {

	@Inject(at=@At("HEAD"), method= "drop(Lnet/minecraft/entity/damage/DamageSource;)V")
	public void dropLoot(DamageSource source, CallbackInfo ci) {
		if (FabConf.isEnabled("*.campfires_cook_entities") && source == DamageSource.IN_FIRE) ((LivingEntity)(Object)this).setFireTicks(1);
	}

}
