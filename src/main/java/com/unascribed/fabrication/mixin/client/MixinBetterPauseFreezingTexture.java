package com.unascribed.fabrication.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;

@Mixin(TextureManager.class)
@EligibleIf(configEnabled="*.better_pause_freezing", envMatches=Env.CLIENT)
public class MixinBetterPauseFreezingTexture {

	@Inject(at=@At("HEAD"), method="tick()V", cancellable=true)
	public void tick(CallbackInfo ci) {
		if (RuntimeChecks.check("*.better_pause_freezing") && MinecraftClient.getInstance().isPaused()) {
			ci.cancel();
		}
	}
	
}
