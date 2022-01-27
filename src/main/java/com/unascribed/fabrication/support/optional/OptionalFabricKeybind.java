package com.unascribed.fabrication.support.optional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;

@Environment(EnvType.CLIENT)
public class OptionalFabricKeybind {
	public static void registerKeyBinding(KeyBinding kb) {
		KeyBindingHelper.registerKeyBinding(kb);
	}
}
