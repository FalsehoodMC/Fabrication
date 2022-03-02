package com.unascribed.fabrication.mixin.b_utility.no_guardian_jumpscare;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.particle.ElderGuardianAppearanceParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;

@Mixin(ElderGuardianAppearanceParticle.class)
@EligibleIf(configAvailable="*.no_guardian_jumpscare", envMatches=Env.CLIENT)
public class MixinElderGuardianAppearanceParticle {

	@Inject(at=@At("HEAD"), method="buildGeometry(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/render/Camera;F)V", cancellable=true)
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta, CallbackInfo ci) {
		if (FabConf.isEnabled("*.no_guardian_jumpscare"))
			ci.cancel();
	}

}
