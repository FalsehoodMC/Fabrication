package com.unascribed.fabrication.client;

import net.minecraft.client.util.math.MatrixStack;

@FunctionalInterface
public interface PrideFlagRenderer {
	void render(MatrixStack matrices, float x, float y, float width, float height);
}
