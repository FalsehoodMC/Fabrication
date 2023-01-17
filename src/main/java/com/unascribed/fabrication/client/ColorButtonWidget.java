package com.unascribed.fabrication.client;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ColorButtonWidget extends ButtonWidget {

	private int bg;

	public ColorButtonWidget(int x, int y, int width, int height, int bg, Text message, PressAction onPress, NarrationSupplier tooltipSupplier) {
		super(x, y, width, height, message, onPress, tooltipSupplier);
		this.bg = bg;
	}

	@Override
	public void drawTexture(MatrixStack matrices, int x, int y, int u, int v, int width, int height) {
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int x = getX(), y = getY();
		fill(matrices, x, y, x+width, y+height, bg);
		if (isHovered() || isFocused()) {
			fill(matrices, x, y, x+width, y+1, -1);
			fill(matrices, x, y, x+1, y+height, -1);
			fill(matrices, x+width-1, y, x+width, y+height, -1);
			fill(matrices, x, y+height-1, x+width, y+height, -1);
		}
	}

}
