package com.unascribed.fabrication.mixin.a_fixes.better_pause_freezing;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.client.AtlasViewerScreen;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureManager;

@Mixin(TextureManager.class)
@EligibleIf(configAvailable="*.better_pause_freezing", envMatches=Env.CLIENT)
public class MixinTextureManager {

	@FabInject(at=@At("HEAD"), method="tick()V", cancellable=true)
	public void tick(CallbackInfo ci) {
		if (FabConf.isEnabled("*.better_pause_freezing") && MinecraftClient.getInstance().isPaused() && !(MinecraftClient.getInstance().currentScreen instanceof AtlasViewerScreen)) {
			ci.cancel();
		}
	}

}
