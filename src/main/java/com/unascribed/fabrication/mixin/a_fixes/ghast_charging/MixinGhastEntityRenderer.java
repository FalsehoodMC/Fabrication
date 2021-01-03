package com.unascribed.fabrication.mixin.a_fixes.ghast_charging;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.GhastAttackTime;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.render.entity.GhastEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.GhastEntity;

@Mixin(GhastEntityRenderer.class)
@EligibleIf(configEnabled="*.ghast_charging", envMatches=Env.CLIENT)
public class MixinGhastEntityRenderer {

	@Inject(at=@At("HEAD"), method="scale(Lnet/minecraft/entity/mob/GhastEntity;Lnet/minecraft/client/util/math/MatrixStack;F)V", cancellable=true)
	public void scale(GhastEntity ghast, MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		if (!RuntimeChecks.check("*.ghast_charging")) return;

		ci.cancel();
		float base = 4.5f;
		float hScale = base;
		float vScale = base;

		if (ghast.isShooting()) {
			// the old code starts 10 ticks before the "shooting" texture is assumed, but we only
			// get a packet once the texture should be assumed. the extra tiny bit of accuracy is
			// not worth needing this tweak on the server-side and sending our own packets; the
			// amount of scaling that occurs in the first 10 ticks is basically unnoticeable. so
			// add the 10 ticks we missed to the counter
			float a = ((((GhastAttackTime)ghast).getAttackTime()+tickDelta)+10) / 20F;
			if (a < 0) a = 0;
			a = 1 / (a * a * a * a * a * 2 + 1);
			hScale = (8 + 1 / a) / 2;
			vScale = (8 + a) / 2;
		}
		matrices.scale(hScale, vScale, hScale);
	}

}
