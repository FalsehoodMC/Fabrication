package com.unascribed.fabrication.mixin.a_fixes.no_night_vision_flash;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

@Mixin(GameRenderer.class)
@EligibleIf(configAvailable="*.no_night_vision_flash", envMatches=Env.CLIENT)
public class MixinGameRenderer {

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/util/math/MathHelper.sin(F)F"),
			method="getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F")
	private static float flash(float f, LivingEntity entity) {
		if (FabConf.isEnabled("*.no_night_vision_flash")) {
			float time = (f/((float)Math.PI*0.2f));
			if (time < 0) time = 0;
			float a = (time/200f);
			a = a*a; // exponential falloff
			return (a-0.7f)/0.3f;
		}
		return MathHelper.sin(f);
	}

}
