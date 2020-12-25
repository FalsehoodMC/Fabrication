package com.unascribed.forgery;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.Enchantment;

@FunctionalInterface
public interface ConfigScreenFactory<S extends Screen> {
	S create(Screen parent);
	
	Enchantment
}