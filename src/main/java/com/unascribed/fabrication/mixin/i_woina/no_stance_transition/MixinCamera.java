package com.unascribed.fabrication.mixin.i_woina.no_stance_transition;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
@EligibleIf(configAvailable="*.no_stance_transition", envMatches=Env.CLIENT)
abstract public class MixinCamera {

	@Shadow
	private float cameraY;

	@Shadow
	private Entity focusedEntity;

	@FabInject(at=@At("HEAD"), method="updateEyeHeight()V")
	public void removeTransition(CallbackInfo ci) {
		if (focusedEntity != null && FabConf.isEnabled("*.no_stance_transition")) {
			cameraY = focusedEntity.getStandingEyeHeight();
		}
	}
}
