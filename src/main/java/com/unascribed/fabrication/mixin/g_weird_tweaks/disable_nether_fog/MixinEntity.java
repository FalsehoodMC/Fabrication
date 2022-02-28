package com.unascribed.fabrication.mixin.g_weird_tweaks.disable_nether_fog;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.Env;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.client.render.SkyProperties;

@Mixin(SkyProperties.Nether.class)
@EligibleIf(configAvailable="*.disable_nether_fog", envMatches=Env.CLIENT)
public class MixinEntity {

	@Inject(at=@At("HEAD"), method= "useThickFog(II)Z", cancellable=true)
	public void useThickFog(int camX, int camY, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.disable_nether_fog"))
			cir.setReturnValue(false);
	}
}
