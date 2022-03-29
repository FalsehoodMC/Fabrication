package com.unascribed.fabrication.client;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.features.FeatureTaggablePlayers;
import io.github.queerbric.pride.PrideFlag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;

import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class TaggablePlayersScreen extends Screen{

	float sidebarScrollTarget;
	float sidebarScroll;
	float sidebarHeight;

	private TextFieldWidget searchField;
	Pattern filter = Pattern.compile("");
	Screen parent;
	PrideFlag prideFlag;
	boolean didClick;


	public TaggablePlayersScreen(Screen parent, PrideFlag prideFlag, String title, String configKey) {
		super(new LiteralText("Fabrication Taggable Players"));
		this.parent = parent;
		this.prideFlag = prideFlag;
	}
	@Override
	protected void init() {
		super.init();
		searchField = new TextFieldWidget(textRenderer, 0, 0, width, 20, searchField, new LiteralText("Filter"));
		searchField.setChangedListener((s) -> {
			s = s.trim();
			filter = Pattern.compile(s, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
		});
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		searchField.render(matrices, mouseX, mouseY, delta);
		float scroll = (float) (Math.floor(((sidebarHeight < height ? 0 : sidebarScroll) * client.getWindow().getScaleFactor())) / client.getWindow().getScaleFactor());
		float y = 22 - scroll;
		for (String key : FeatureTaggablePlayers.validTags.keySet()) {
			if (!filter.matcher(key).find()) continue;
			boolean isActive = FeatureTaggablePlayers.activeTags.containsKey(key);
			textRenderer.drawWithShadow(matrices, key, 5, y, isActive ? FabConf.isEnabled(key) ? -1 : 0xffff2222 : 0xffaaaaaa);
			if (isActive) {
				int val = FeatureTaggablePlayers.activeTags.get(key);
				if (drawToggleButton(matrices, width-160, (int)y, 45, 10, "Invert", mouseX, mouseY, (val & 0b1) != 0)) {
					FeatureTaggablePlayers.add(key, val ^ 0b1);
				} else if (drawToggleButton(matrices, width-100, (int)y, 90, 10, "Player Exclusive", mouseX, mouseY, (val & 0b10) == 0)) {
					FeatureTaggablePlayers.add(key, val ^ 0b10);
				} else if (didClick && mouseY > y && mouseY < y + 12) {
					FeatureTaggablePlayers.remove(key);
				}
			} else if (didClick && mouseY > y && mouseY < y + 12) {
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
				FeatureTaggablePlayers.add(key, 0);
			}

			y += 12;
			if (y > height) break;
		}
		sidebarHeight = FeatureTaggablePlayers.validTags.size() * 12 + 8;

		if (didClick) didClick = false;
	}

	private boolean drawToggleButton(MatrixStack matrices, int x, int y, int w, int h, String text, float mouseX, float mouseY, boolean toggle) {
		boolean click = false;
		boolean hover = mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h;
		if (hover ^ toggle) {
			fill(matrices, x, y, x + w, y + 1, -1);
			fill(matrices, x, y, x + 1, y + h, -1);
			fill(matrices, x, y + h - 1, x + w, y + h, -1);
			fill(matrices, x + w - 1, y, x + w, y + h, -1);
		}
		if (hover && didClick) {
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
			click = true;
		}
		int textWidth = textRenderer.getWidth(text);
		textRenderer.draw(matrices, text, x+((w-textWidth)/2f), y+((h-8)/2f), -1);
		return click;
	}

	@Override
	public void tick() {
		super.tick();
		if (sidebarHeight > height) {
			sidebarScroll += (sidebarScrollTarget-sidebarScroll)/2;
			if (sidebarScrollTarget < 0) sidebarScrollTarget /= 2;
			float h = sidebarHeight-height;
			if (sidebarScrollTarget > h) sidebarScrollTarget = h+((sidebarScrollTarget-h)/2);
		}
	}

	@Override
	public void renderBackground(MatrixStack matrices) {
		FabricationConfigScreen.drawBackground(height, width, client, prideFlag, 0, matrices, 0, 0, 0, 0, 0);
	}

	@Override
	public void onClose() {
		client.setScreen(parent);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			didClick = true;
		}
		searchField.mouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		sidebarScrollTarget -= amount * 20;
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		searchField.keyPressed(keyCode, scanCode, modifiers);
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		searchField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		searchField.mouseMoved(mouseX, mouseY);
		super.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		searchField.mouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		searchField.charTyped(chr, modifiers);
		return super.charTyped(chr, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		searchField.keyReleased(keyCode, scanCode, modifiers);
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

}
