package com.unascribed.fabrication.mixin.i_woina.no_experience;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.screen.AnvilScreenHandler;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configAvailable="*.no_experience", envMatches=Env.CLIENT)
public class MixinAnvilScreenHandlerClient {

	@Inject(at=@At("HEAD"), method="getLevelCost()I", cancellable=true)
	public void getLevelCost(CallbackInfoReturnable<Integer> ci) {
		if (FabConf.isEnabled("*.no_experience")) {
			ci.setReturnValue(0);
		}
	}

}
