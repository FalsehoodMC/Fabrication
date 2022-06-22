package com.unascribed.fabrication.mixin.g_weird_tweaks.disable_nether_fog;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

@Mixin(targets="net.minecraft.client.render.DimensionEffects$Nether")
@EligibleIf(configAvailable="*.disable_nether_fog", envMatches=Env.CLIENT)
public class MixinEntity {

	@FabInject(at=@At("HEAD"), method= "useThickFog(II)Z", cancellable=true)
	public void useThickFog(int camX, int camY, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.disable_nether_fog"))
			cir.setReturnValue(false);
	}
}
