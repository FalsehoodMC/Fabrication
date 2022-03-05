package com.unascribed.fabrication.client;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.loaders.LoaderYeetRecipes;
import io.github.queerbric.pride.PrideFlag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.Iterator;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class YeetRecipesScreen extends Screen{

	float sidebarScrollTarget;
	float sidebarScroll;
	float sidebarHeight;
	float sidebar2ScrollTarget;
	float sidebar2Scroll;
	float sidebar2Height;

	private TextFieldWidget searchField;
	Pattern filter = Pattern.compile("");
	Screen parent;
	PrideFlag prideFlag;
	boolean didClick;
	boolean didRClick;


	public YeetRecipesScreen(Screen parent, PrideFlag prideFlag, String title, String configKey) {
		super(new LiteralText("Fabrication Yeet Recipes"));
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
		fill(matrices, 0, 22, width, height / 2+10, FabConf.isEnabled("general.dark_mode") ? 0x44FFFFFF : 0x55000000);
		searchField.render(matrices, mouseX, mouseY, delta);
		if (client.world == null) {
			textRenderer.drawWithShadow(matrices, "Load a world to see suggestions", 5, 25, -1);
		} else {
			float scroll = sidebar2Height < height ? 0 : sidebar2Scroll;
			scroll = (float) (Math.floor((scroll * client.getWindow().getScaleFactor())) / client.getWindow().getScaleFactor());
			float y = 22 - scroll;
			for (Recipe<?> clr : client.world.getRecipeManager().values()) {
				if (!filter.matcher(clr.getId().toString()).find()) continue;
				if (y > 0) {
					textRenderer.drawWithShadow(matrices, clr.getId().toString(), 5, y, -1);
				}
				if (didClick && mouseY > 22 && mouseY > y && mouseY < y + 12) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
					LoaderYeetRecipes.recipesToYeet.add(clr.getId());
					String adding = clr.getId().toString();
					int col = adding.indexOf(':');
					LoaderYeetRecipes.instance.set(col==-1? adding : (adding.substring(0, ++col)+"."+adding.substring(col)), "true");
				}
				y += 12;
				if (y > height/2f) break;
			}
			sidebar2Height = client.world.getRecipeManager().values().stream().filter(r->filter.matcher(r.getId().toString()).find()).count() * 12 + 8;

		}
		float scroll = sidebarHeight < height ? 0 : sidebarScroll;
		scroll = (float) (Math.floor((scroll * client.getWindow().getScaleFactor())) / client.getWindow().getScaleFactor());
		float y = 10 + height/2f - scroll;
		Iterator<Identifier> iter = LoaderYeetRecipes.recipesToYeet.iterator();
		while (iter.hasNext()) {
			Identifier clr = iter.next();
			if (y > height/2f) {
				textRenderer.drawWithShadow(matrices, clr.toString(), 5, y, -1);
			}
			if (didRClick && mouseY > height/2 && mouseY > y && mouseY < y + 12) {
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
				iter.remove();
				LoaderYeetRecipes.instance.remove(clr.toString());
			}
			y += 12;
			if (y > height) break;
		}
		sidebarHeight = LoaderYeetRecipes.recipesToYeet.size() * 12 + 8;

		if (didClick) didClick = false;
		if (didRClick) didRClick = false;
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
		if (sidebar2Height > height) {
			sidebar2Scroll += (sidebar2ScrollTarget-sidebar2Scroll)/2;
			if (sidebar2ScrollTarget < 0) sidebar2ScrollTarget /= 2;
			float h = sidebar2Height-height;
			if (sidebar2ScrollTarget > h) sidebar2ScrollTarget = h+((sidebar2ScrollTarget-h)/2);
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
		} else if (button == 1){
			didRClick = true;
		}
		searchField.mouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (mouseY > height/2d) {
			sidebarScrollTarget -= amount * 20;
		} else {
			sidebar2ScrollTarget -= amount * 20;
		}
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
