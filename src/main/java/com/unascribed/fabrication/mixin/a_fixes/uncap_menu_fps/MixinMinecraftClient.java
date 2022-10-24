package com.unascribed.fabrication.mixin.a_fixes.uncap_menu_fps;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
@EligibleIf(configAvailable="*.uncap_menu_fps", envMatches=Env.CLIENT)
public class MixinMinecraftClient {

	@FabInject(at=@At("HEAD"), method="getFramerateLimit()I", cancellable=true)
	private void getFramerateLimit(CallbackInfoReturnable<Integer> ci) {
		if (FabConf.isEnabled("*.uncap_menu_fps")) {
			ci.setReturnValue(((MinecraftClient)(Object)this).getWindow().getFramerateLimit());
		}
	}

}
