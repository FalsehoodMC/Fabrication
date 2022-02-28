package com.unascribed.fabrication.client;

import io.github.queerbric.pride.PrideFlag;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class BlockLogoScreen extends Screen{
	Screen parent;
	PrideFlag prideFlag;

	public BlockLogoScreen(Screen parent, PrideFlag prideFlag, String title, String configKey) {
		super(new LiteralText("Fabrication Block Logo"));
		this.parent = parent;
		this.prideFlag = prideFlag;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
	}

	@Override
	public void renderBackground(MatrixStack matrices) {
		FabricationConfigScreen.drawBackground(height, width, client, prideFlag, 0, matrices, 0, 0, 0, 0, 0);
	}

	@Override
	public void onClose() {
		client.setScreen(parent);
	}
}
