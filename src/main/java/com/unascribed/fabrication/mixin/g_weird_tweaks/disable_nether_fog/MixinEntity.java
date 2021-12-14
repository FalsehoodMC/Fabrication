package com.unascribed.fabrication.mixin.g_weird_tweaks.disable_nether_fog;

import com.unascribed.fabrication.support.Env;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.render.DimensionEffects;

@Mixin(DimensionEffects.Nether.class)
@EligibleIf(configAvailable="*.disable_nether_fog", envMatches=Env.CLIENT)
public class MixinEntity {

	@Inject(at=@At("HEAD"), method= "useThickFog(II)Z", cancellable=true)
	public void useThickFog(int camX, int camY, CallbackInfoReturnable<Boolean> cir) {
		if (MixinConfigPlugin.isEnabled("*.disable_nether_fog"))
			cir.setReturnValue(false);
	}
}
