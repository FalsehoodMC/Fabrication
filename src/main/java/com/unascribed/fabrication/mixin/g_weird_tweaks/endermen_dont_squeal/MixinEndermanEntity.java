package com.unascribed.fabrication.mixin.g_weird_tweaks.endermen_dont_squeal;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

@Mixin(EndermanEntity.class)
@EligibleIf(configAvailable="*.endermen_dont_squeal")
public class MixinEndermanEntity {

	@FabInject(at=@At("HEAD"), method="playAngrySound()V", cancellable=true)
	public void playAngrySound(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.endermen_dont_squeal")) return;
		ci.cancel();
	}

	@FabInject(at=@At("HEAD"), method="getAmbientSound()Lnet/minecraft/sound/SoundEvent;", cancellable=true)
	public void getAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
		if (!FabConf.isEnabled("*.endermen_dont_squeal")) return;
		cir.setReturnValue(SoundEvents.ENTITY_ENDERMAN_AMBIENT);
	}

}
