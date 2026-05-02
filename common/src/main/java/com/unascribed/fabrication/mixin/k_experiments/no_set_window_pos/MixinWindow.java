package com.unascribed.fabrication.mixin.k_experiments.no_set_window_pos;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
@EligibleIf(configAvailable="*.no_set_window_pos", envMatches=Env.CLIENT)
public class MixinWindow {

	@Shadow
	private boolean fullscreen;

	@FabInject(at=@At("HEAD"), method="updateWindowRegion()V", cancellable=true)
	public void updateWindowRegion(CallbackInfo ci) {
		if (FabConf.isEnabled("*.no_set_window_pos") && !fullscreen) {
			ci.cancel();
		}
	}

}
