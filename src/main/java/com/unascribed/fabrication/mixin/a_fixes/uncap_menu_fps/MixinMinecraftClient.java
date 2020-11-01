package com.unascribed.fabrication.mixin.a_fixes.uncap_menu_fps;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
@EligibleIf(configEnabled="*.uncap_menu_fps", envMatches=Env.CLIENT)
public class MixinMinecraftClient {

	@Inject(at=@At("HEAD"), method="getFramerateLimit()I", cancellable=true)
	private void getFramerateLimit(CallbackInfoReturnable<Integer> ci) {
		if (RuntimeChecks.check("*.uncap_menu_fps")) {
			ci.setReturnValue(((MinecraftClient)(Object)this).getWindow().getFramerateLimit());
		}
	}
	
}
