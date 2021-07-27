package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Option;
import net.minecraft.client.util.InputUtil;

@EligibleIf(configEnabled="*.toggle_sprint", envMatches=Env.CLIENT)
public class FeatureToggleSprint implements Feature {

	public static KeyBinding keybind;
	public static boolean sprinting = false;

	@Override
	public void apply() {
		keybind = new KeyBinding("[Fabrication] Toggle/Hold Sprint", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.movement") {
			@Override
			public void setPressed(boolean pressed) {
				if (!pressed && MinecraftClient.getInstance().getNetworkHandler() == null) {
					// reset() was probably called, so, reset
					sprinting = false;
				}
				if(MinecraftClient.getInstance().options.sprintToggled){
					sprinting = pressed;
				}else if (!isPressed() && pressed) {
					sprinting = !sprinting;
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
