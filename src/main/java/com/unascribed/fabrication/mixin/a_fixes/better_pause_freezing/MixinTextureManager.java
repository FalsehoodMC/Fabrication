package com.unascribed.fabrication.mixin.a_fixes.better_pause_freezing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.client.AtlasViewerScreen;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;

@Mixin(TextureManager.class)
@EligibleIf(configAvailable="*.better_pause_freezing", envMatches=Env.CLIENT)
public class MixinTextureManager {

	@Inject(at=@At("HEAD"), method="tick()V", cancellable=true)
	public void tick(CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.better_pause_freezing") && MinecraftClient.getInstance().isPaused() && !(MinecraftClient.getInstance().currentScreen instanceof AtlasViewerScreen)) {
			ci.cancel();
		}
	}
	
}
