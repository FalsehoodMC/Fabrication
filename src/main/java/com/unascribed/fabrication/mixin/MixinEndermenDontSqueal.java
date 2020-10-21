package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.OnlyIf;

import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

@Mixin(EndermanEntity.class)
@OnlyIf(config="tweaks.endermen_dont_squeal")
public class MixinEndermenDontSqueal {

	@Inject(at=@At("HEAD"), method="playAngrySound()V", cancellable=true)
	public void playAngrySound(CallbackInfo ci) {
		ci.cancel();
	}
	
	@Inject(at=@At("HEAD"), method="getAmbientSound()Lnet/minecraft/sound/SoundEvent;", cancellable=true)
	public void getAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
		cir.setReturnValue(SoundEvents.ENTITY_ENDERMAN_AMBIENT);
	}
	
}
