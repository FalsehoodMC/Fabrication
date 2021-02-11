package com.unascribed.fabrication.mixin.j_experiments.no_set_window_pos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.util.Window;

@Mixin(Window.class)
@EligibleIf(configEnabled="*.no_set_window_pos", envMatches=Env.CLIENT)
public class MixinWindow {

	@Shadow
	private boolean fullscreen;
	
	@Inject(at=@At("HEAD"), method="updateWindowRegion()V", cancellable=true)
	public void updateWindowRegion(CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.no_set_window_pos") && !fullscreen) {
			ci.cancel();
		}
	}
	
}
