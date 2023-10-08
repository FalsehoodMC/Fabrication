package com.unascribed.fabrication.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ColorButtonWidget extends ButtonWidget {

	private int bg;

	public ColorButtonWidget(int x, int y, int width, int height, int bg, Text message, PressAction onPress, NarrationSupplier tooltipSupplier) {
		super(x, y, width, height, message, onPress, tooltipSupplier);
		this.bg = bg;
	}

	@Override
	public void renderButton(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		int x = getX(), y = getY();
		drawContext.fill(x, y, x+width, y+height, bg);
		if (isHovered() || isFocused()) {
			drawContext.fill(x, y, x+width, y+1, -1);
			drawContext.fill(x, y, x+1, y+height, -1);
			drawContext.fill(x+width-1, y, x+width, y+height, -1);
			drawContext.fill(x, y+height-1, x+width, y+height, -1);
		}
	}

}
