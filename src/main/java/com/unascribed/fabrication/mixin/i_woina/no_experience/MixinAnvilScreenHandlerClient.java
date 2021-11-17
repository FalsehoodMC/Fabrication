package com.unascribed.fabrication.mixin.i_woina.no_experience;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.screen.AnvilScreenHandler;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configAvailable="*.no_experience", envMatches=Env.CLIENT)
public class MixinAnvilScreenHandlerClient {

	@Inject(at=@At("HEAD"), method="getLevelCost()I", cancellable=true)
	public void getLevelCost(CallbackInfoReturnable<Integer> ci) {
		if (MixinConfigPlugin.isEnabled("*.no_experience")) {
			ci.setReturnValue(0);
		}
	}

}
