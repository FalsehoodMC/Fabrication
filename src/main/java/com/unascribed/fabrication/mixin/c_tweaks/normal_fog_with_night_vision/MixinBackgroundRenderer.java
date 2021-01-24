package com.unascribed.fabrication.mixin.c_tweaks.normal_fog_with_night_vision;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.render.BackgroundRenderer;

@Mixin(BackgroundRenderer.class)
@EligibleIf(configEnabled="*.normal_fog_with_night_vision", envMatches=Env.CLIENT)
public class MixinBackgroundRenderer {

	@ModifyVariable(at=@At(value="INVOKE_ASSIGN", target="net/minecraft/client/render/GameRenderer.getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F",
				shift=Shift.AFTER),
			method="render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V", ordinal=2)
	private static float getNightVisionStrength(float orig) {
		if (MixinConfigPlugin.isEnabled("*.normal_fog_with_night_vision")) {
			return 0;
		}
		return orig;
	}
	
}
