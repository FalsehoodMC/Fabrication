package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

@EligibleIf(configEnabled="*.toggle_sprint", envMatches=Env.CLIENT)
public class FeatureToggleSprint implements Feature {

	public static KeyBinding keybind;
	public static boolean sprinting = false;
	public static int toggleTime = 1000;

	@Override
	public void apply() {
		keybind = new KeyBinding("[Fabrication] Toggle Sprint", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.movement") {
			@Override
			public void setPressed(boolean pressed) {
				if (!pressed && MinecraftClient.getInstance().getNetworkHandler() == null) {
					// reset() was probably called, so, reset
					sprinting = false;
				}
				if (!isPressed() && pressed) {
					sprinting = !sprinting;
					toggleTime = 0;
				}
				super.setPressed(pressed);
			}
		};
		Agnos.registerKeyBinding(keybind);
	}

	@Override
	public boolean undo() {
		return false;
	}

	@Override
	public String getConfigKey() {
		return "*.toggle_sprint";
	}
	
}
