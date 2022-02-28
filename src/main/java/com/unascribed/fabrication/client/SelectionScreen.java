package com.unascribed.fabrication.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
class SelectionScreen<T> extends Screen {
	final Screen parent;
	final List<? extends PreciseDrawable> features;
	final Consumer<T> out;
	float sidebarScrollTarget;
	float sidebarScroll;
	float sidebarHeight;
	boolean didClick = false;

	public SelectionScreen(Screen parent, List<?> options, Consumer<T> out) {
		super(parent.getTitle());
		this.out = out;
		this.parent = parent;
		this.features = options.get(0) instanceof PreciseDrawable ? (List<? extends PreciseDrawable>) options : options.stream().map(TextWidget::new).collect(Collectors.toList());
	}

	public class TextWidget implements PreciseDrawable {
		final Object o;
		final String text;

		public TextWidget(Object o){
			this.o = o;
			this.text = o.toString();
		}

		@Override
		public void render(MatrixStack matrices, float x, float y, float delta) {
			textRenderer.drawWithShadow(matrices, text, x, y, -1);
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
		public Object val(){
			return o;
		}
	}

	public interface PreciseDrawable {
		void render(MatrixStack matrices, float x, float y, float delta);
		int width();
		int height();
		Object val();
	}


	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		parent.height = this.height;
		parent.width = this.width;
		parent.renderBackground(matrices);
		float scroll = sidebarHeight < height ? 0 : sidebarScroll;
		sidebarHeight = 20;
		scroll = (float) (Math.floor((scroll * client.getWindow().getScaleFactor())) / client.getWindow().getScaleFactor());
		float y = 22 - scroll;
		for (PreciseDrawable feature : features) {
			feature.render(matrices, 16, y, delta);
			int height = feature.height();
			if (mouseY > y - 2 && mouseY < y + height) {
				fill(matrices, 0, (int) y+height, feature.width()+16, (int) y+height+1, -1);
				if (didClick) {
					out.accept((T) feature.val());
					onClose();
				}
			}
			sidebarHeight += 9+height;
			y += 9+height;
		}

	}

	@Override
	public void tick() {
		super.tick();
		if (sidebarHeight > height) {
			sidebarScroll += (sidebarScrollTarget - sidebarScroll) / 2;
			if (sidebarScrollTarget < 0) sidebarScrollTarget /= 2;
			float h = sidebarHeight - height;
			if (sidebarScrollTarget > h) sidebarScrollTarget = h + ((sidebarScrollTarget - h) / 2);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		sidebarScrollTarget -= amount * 20;
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	@Override
	public void onClose() {
		client.setScreen(parent);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 || button == 1) didClick = true;
		return super.mouseClicked(mouseX, mouseY, button);
	}

}
