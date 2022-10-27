package com.unascribed.fabrication.client;

import com.unascribed.fabrication.loaders.LoaderClassicBlockDrops;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class ClassicBlockDropsScreen extends Screen{

	final ScrollBar dropsBar = new ScrollBar(height-100);
	final ScrollBar addItemBar = new ScrollBar(80);

	private TextFieldWidget searchField;
	Pattern filter = Pattern.compile("");
	Screen parent;
	PrideFlagRenderer prideFlag;
	boolean didClick;
	boolean didRClick;


	public ClassicBlockDropsScreen(Screen parent, PrideFlagRenderer prideFlag, String title, String configKey) {
		super(new LiteralText("Fabrication Yeet Recipes"));
		this.parent = parent;
		this.prideFlag = prideFlag;
	}
	@Override
	protected void init() {
		super.init();
		searchField = new TextFieldWidget(textRenderer, 0, 80, width, 20, searchField, new LiteralText("Filter"));
		searchField.setChangedListener((s) -> {
			s = s.trim();
			filter = Pattern.compile(s, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
		});
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		searchField.render(matrices, mouseX, mouseY, delta);
		{
			float scroll = -addItemBar.getScaledScroll(client);
			for (Identifier clr : Registry.ITEM.getIds()) {
				if (!filter.matcher(clr.toString()).find()) continue;
				if (scroll > 0) {
					textRenderer.drawWithShadow(matrices, clr.toString(), 5, scroll, -1);
				}
				if (didClick && mouseY > scroll && mouseY < scroll + 12) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
					LoaderClassicBlockDrops.literals.put(clr.toString(), Optional.of(true));
					LoaderClassicBlockDrops.instance.reload();
					LoaderClassicBlockDrops.instance.set(clr.toString(), "true");
				}
				scroll += 12;
				if (scroll > 70) break;
			}
			addItemBar.height = Registry.ITEM.getIds().stream().filter(r->filter.matcher(r.toString()).find()).count() * 12 + 20;
		}
		float scroll = 102 - dropsBar.getScaledScroll(client);
		Iterator<Map.Entry<String, Optional<Boolean>>> iter = LoaderClassicBlockDrops.heuristics.entrySet().iterator();
		boolean firstPass = true;
		while (true) {
			Map.Entry<String, Optional<Boolean>> entry = iter.next();
			if (scroll > 102) {
				String k = entry.getKey();
				Optional<Boolean> val = entry.getValue();
				textRenderer.drawWithShadow(matrices, k, 5, scroll, val.isPresent() ? -1 : 0xffff2222);
				if (!val.isPresent() && drawButton(matrices, width - 80, (int) scroll, 45, 10, "Reset", mouseX, mouseY)) {
					entry.setValue(Optional.of(true));
					LoaderClassicBlockDrops.instance.reload();
					LoaderClassicBlockDrops.instance.set(firstPass ? "@heuristics." + k : k, "true");
				} else if (val.isPresent() && drawToggleButton(matrices, width - 80, (int) scroll, 45, 10, "Tiled", mouseX, mouseY, val.get())) {
					boolean inverted = !val.get();
					entry.setValue(Optional.of(inverted));
					LoaderClassicBlockDrops.instance.reload();
					LoaderClassicBlockDrops.instance.set(firstPass ? "@heuristics." + k : k, String.valueOf(inverted));
				} else if (didRClick && mouseY > scroll && mouseY < scroll + 12) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
					iter.remove();
					LoaderClassicBlockDrops.instance.remove(firstPass ? "@heuristics." + k : k);
				}
			}
			scroll += 12;
			if (scroll > height) break;

			if (!iter.hasNext()){
				if (firstPass) {
					firstPass = false;
					scroll += 20;
					iter = LoaderClassicBlockDrops.literals.entrySet().iterator();
				} else {
					break;
				}
			}
		}
		dropsBar.height = LoaderClassicBlockDrops.heuristics.size() * 12 + LoaderClassicBlockDrops.literals.size() * 12 + 40;

		if (didClick) didClick = false;
		if (didRClick) didRClick = false;
	}

	private boolean drawButton(MatrixStack matrices, int x, int y, int w, int h, String text, float mouseX, float mouseY) {
		return FabricationConfigScreen.drawButton(matrices, x, y, w, h, text, mouseX, mouseY, didClick, client);
	}

	private boolean drawToggleButton(MatrixStack matrices, int x, int y, int w, int h, String text, float mouseX, float mouseY, boolean toggle) {
		return FabricationConfigScreen.drawToggleButton(matrices, x, y, w, h, text, mouseX, mouseY, toggle, didClick, client);
	}

	@Override
	public void tick() {
		super.tick();
		dropsBar.tick();
		addItemBar.tick();
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
		} else if (button == 1){
			didRClick = true;
		}
		searchField.mouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (mouseY > 90) {
			dropsBar.scroll(amount*20);
		} else {
			addItemBar.scroll(amount*20);
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			String input = searchField.getText();
			if (searchField.isActive() && !input.isEmpty()) {
				LoaderClassicBlockDrops.heuristics.put(input, Optional.of(true));
				LoaderClassicBlockDrops.instance.reload();
				LoaderClassicBlockDrops.instance.set("@heuristics." +input, "true");
				searchField.setText("");
				return super.keyPressed(keyCode, scanCode, modifiers);
			}
		}
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
		dropsBar.displayHeight = height-100;
		super.resize(client, width, height);
	}

}
