package com.unascribed.fabrication;

import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface ConfigScreenFactory<S extends Screen> {
	S create(Screen parent);
}