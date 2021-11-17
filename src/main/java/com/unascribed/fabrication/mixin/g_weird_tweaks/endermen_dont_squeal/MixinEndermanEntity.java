package com.unascribed.fabrication.mixin.g_weird_tweaks.endermen_dont_squeal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

@Mixin(EndermanEntity.class)
@EligibleIf(configAvailable="*.endermen_dont_squeal")
public class MixinEndermanEntity {

	@Inject(at=@At("HEAD"), method="playAngrySound()V", cancellable=true)
	public void playAngrySound(CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.endermen_dont_squeal")) return;
		ci.cancel();
	}

	@Inject(at=@At("HEAD"), method="getAmbientSound()Lnet/minecraft/sound/SoundEvent;", cancellable=true)
	public void getAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
		if (!MixinConfigPlugin.isEnabled("*.endermen_dont_squeal")) return;
		cir.setReturnValue(SoundEvents.ENTITY_ENDERMAN_AMBIENT);
	}

}
