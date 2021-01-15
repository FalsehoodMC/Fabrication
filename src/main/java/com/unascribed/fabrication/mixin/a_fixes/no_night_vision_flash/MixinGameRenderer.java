package com.unascribed.fabrication.mixin.a_fixes.no_night_vision_flash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;

@Mixin(GameRenderer.class)
@EligibleIf(configEnabled="*.no_night_vision_flash", envMatches=Env.CLIENT)
public class MixinGameRenderer {

	@Inject(at=@At("HEAD"), method="getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F", cancellable=true)
	private static void getNightVisionStrength(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> ci) {
		if (MixinConfigPlugin.isEnabled("*.no_night_vision_flash")) {
			int duration = entity.getStatusEffect(StatusEffects.NIGHT_VISION).getDuration();
			if (duration < 200) {
				ci.setReturnValue(duration/200f);
			} else {
				ci.setReturnValue(1f);
			}
		}
	}
	
}
