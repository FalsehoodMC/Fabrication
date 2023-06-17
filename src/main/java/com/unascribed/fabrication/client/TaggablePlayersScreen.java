package com.unascribed.fabrication.client;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.features.FeatureTaggablePlayers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class TaggablePlayersScreen extends Screen{

	final ScrollBar scrollBar = new ScrollBar(height);

	private TextFieldWidget searchField;
	Pattern filter = Pattern.compile("");
	Screen parent;
	PrideFlagRenderer prideFlag;
	boolean didClick;


	public TaggablePlayersScreen(Screen parent, PrideFlagRenderer prideFlag, String title, String configKey) {
		super(Text.literal("Fabrication Taggable Players"));
		this.parent = parent;
		this.prideFlag = prideFlag;
	}
	@Override
	protected void init() {
		super.init();
		searchField = new TextFieldWidget(textRenderer, 0, 0, width, 20, searchField, Text.literal("Filter"));
		searchField.setChangedListener((s) -> {
			s = s.trim();
			filter = Pattern.compile(s, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
		});
	}

	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		renderBackground(drawContext);
		searchField.render(drawContext, mouseX, mouseY, delta);
		float scroll = scrollBar.getScaledScroll(client);
		float y = 22 - scroll;
		for (Map.Entry<String, Integer> entry : FeatureTaggablePlayers.validTags.entrySet()) {
			String key = entry.getKey();
			if (!filter.matcher(key).find()) continue;
			if (y>22) {
				boolean isActive = FeatureTaggablePlayers.activeTags.containsKey(key);
				drawContext.drawText(textRenderer, key, 5, (int) y, isActive ? FabConf.isEnabled(key) ? -1 : 0xffff2222 : 0xffaaaaaa, true);
				if (isActive) {
					int val = FeatureTaggablePlayers.activeTags.get(key);
					int mask = entry.getValue();
					if ((mask & 0b1) != 0 && drawToggleButton(drawContext, width - 160, (int) y, 45, 10, "Invert", mouseX, mouseY, (val & 0b1) != 0)) {
						FeatureTaggablePlayers.add(key, val ^ 0b1);
					} else if ((mask & 0b10) != 0 && drawToggleButton(drawContext, width - 100, (int) y, 90, 10, "Player Exclusive", mouseX, mouseY, (val & 0b10) == 0)) {
						FeatureTaggablePlayers.add(key, val ^ 0b10);
					} else if (didClick && mouseY > y && mouseY < y + 12) {
						FeatureTaggablePlayers.remove(key);
					}
				} else if (didClick && mouseY > y && mouseY < y + 12) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
					FeatureTaggablePlayers.add(key, 0);
				}
			}

			y += 12;
			if (y > height) break;
		}
		scrollBar.height = FeatureTaggablePlayers.validTags.size() * 12 + 8;

		if (didClick) didClick = false;
	}

	private boolean drawToggleButton(DrawContext matrices, int x, int y, int w, int h, String text, float mouseX, float mouseY, boolean toggle) {
		return FabricationConfigScreen.drawToggleButton(matrices, x, y, w, h, text, mouseX, mouseY, toggle, didClick, client);
	}

	@Override
	public void tick() {
		super.tick();
		scrollBar.tick();
	}

	@Override
	public void renderBackground(DrawContext matrices) {
		FabricationConfigScreen.drawBackground(height, width, client, prideFlag, 0, matrices, 0, 0, 0, 0, 0);
	}

	@Override
	public void close() {
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
		scrollBar.scroll(amount * 20);
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

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		scrollBar.displayHeight = height;
		super.resize(client, width, height);
	}

}
