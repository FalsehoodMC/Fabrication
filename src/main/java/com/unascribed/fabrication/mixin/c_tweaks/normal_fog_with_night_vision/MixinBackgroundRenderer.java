package com.unascribed.fabrication.mixin.c_tweaks.normal_fog_with_night_vision;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BackgroundRenderer.class)
@EligibleIf(configAvailable="*.normal_fog_with_night_vision", envMatches=Env.CLIENT)
public class MixinBackgroundRenderer {

	@ModifyReturn(target="net/minecraft/client/render/GameRenderer.getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F",
		method="render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V")
	private static float getNightVisionStrength(float orig) {
		if (FabConf.isEnabled("*.normal_fog_with_night_vision")) {
			return 0;
		}
		return orig;
	}

}
