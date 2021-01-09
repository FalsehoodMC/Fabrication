package com.unascribed.fabrication.mixin.c_tweaks.normal_fog_with_night_vision;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

@Mixin(BackgroundRenderer.class)
@EligibleIf(configEnabled="*.normal_fog_with_night_vision", envMatches=Env.CLIENT)
public class MixinBackgroundRenderer {

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/entity/LivingEntity.hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"),
			method="render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V")
	private static boolean hasStatusEffect(LivingEntity subject, StatusEffect effect) {
		if (RuntimeChecks.check("*.normal_fog_with_night_vision") && effect == StatusEffects.NIGHT_VISION) {
			return false;
		}
		return subject.hasStatusEffect(effect);
	}
	
}
