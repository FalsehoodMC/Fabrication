package com.unascribed.fabrication.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
class SelectionScreen<T> extends Screen {
	final Screen parent;
	final List<? extends PreciseDrawable<T>> features;
	final Consumer<T> out;
	final ScrollBar scrollBar = new ScrollBar(height);
	boolean didClick = false;

	public SelectionScreen(Screen parent, List<? extends PreciseDrawable<T>> options, Consumer<T> out) {
		super(parent.getTitle());
		this.out = out;
		this.parent = parent;
		this.features = options;
	}

	public class TextWidget<T> implements PreciseDrawable<T> {
		final T o;
		final String text;

		public TextWidget(T o){
			this.o = o;
			this.text = o.toString();
		}

		@Override
		public void render(DrawContext drawContext, float x, float y, float delta) {
			drawContext.drawText(textRenderer, text, (int) x, (int) y, -1, true);
		}

		@Override
		public int width() {
			return textRenderer.getWidth(text);
		}

		@Override
		public int height() {
			return 11;
		}

		@Override
		public T val(){
			return o;
		}
	}

	public interface PreciseDrawable<T> {
		void render(DrawContext drawContext, float x, float y, float delta);
		int width();
		int height();
		T val();
	}


	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		parent.height = this.height;
		parent.width = this.width;
		parent.renderBackground(drawContext);
		float scroll = scrollBar.getScaledScroll(client);
		scrollBar.height = 20;
		scroll = (float) (Math.floor((scroll * client.getWindow().getScaleFactor())) / client.getWindow().getScaleFactor());
		float y = 22 - scroll;
		for (PreciseDrawable<?> feature : features) {
			feature.render(drawContext, 16, y, delta);
			int height = feature.height();
			if (mouseY > y - 2 && mouseY < y + height) {
				drawContext.fill(0, (int) y+height, feature.width()+16, (int) y+height+1, -1);
				if (didClick) {
					out.accept((T) feature.val());
					close();
				}
			}
			scrollBar.height += 9+height;
			y += 9+height;
		}

	}

	@Override
	public void tick() {
		super.tick();
		scrollBar.tick();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		scrollBar.scroll(amount * 20);
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 || button == 1) didClick = true;
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		scrollBar.displayHeight = height;
		super.resize(client, width, height);
	}

}
