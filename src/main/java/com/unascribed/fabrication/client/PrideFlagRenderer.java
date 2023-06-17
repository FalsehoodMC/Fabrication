package com.unascribed.fabrication.client;

import net.minecraft.client.gui.DrawContext;

@FunctionalInterface
public interface PrideFlagRenderer {
	void render(DrawContext drawContext, float x, float y, float width, float height);
}
