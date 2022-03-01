package com.unascribed.fabrication.client;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.loaders.LoaderBlockLogo;
import com.unascribed.fabrication.util.BlockLogoRenderer;
import io.github.queerbric.pride.PrideFlag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Environment(EnvType.CLIENT)
public class BlockLogoScreen extends Screen{
	int selected = 0;
	int num = 0;
	float sidebarScrollTarget;
	float sidebarScroll;
	float sidebarHeight;
	float sidebar2ScrollTarget;
	float sidebar2Scroll;
	float sidebar2Height;
	int startY = LoaderBlockLogo.image.getHeight()+90;

	final Set<Identifier> registryBlocks = Registry.BLOCK.getIds();
	Integer selectedColor = null;
	BlockLogoRenderer blockLogo = new BlockLogoRenderer();
	StringBuilder filter = new StringBuilder();
	Screen parent;
	PrideFlag prideFlag;
	boolean didClick;
	boolean didRClick;


	public BlockLogoScreen(Screen parent, PrideFlag prideFlag, String title, String configKey) {
		super(new LiteralText("Fabrication Block Logo"));
		this.parent = parent;
		this.prideFlag = prideFlag;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		fill(matrices, 0, 0, width, 30, FabConf.isEnabled("general.dark_mode") ? 0x44FFFFFF : 0x55000000);
		fill(matrices, 0, startY, width/2, height, FabConf.isEnabled("general.dark_mode") ? 0x44FFFFFF : 0x55000000);
		if (drawToggleButton(matrices, 5, 5, 60, 20, "Sound", mouseX, mouseY, LoaderBlockLogo.sound)){
			LoaderBlockLogo.sound = !LoaderBlockLogo.sound;
			LoaderBlockLogo.set("general.sound", String.valueOf(LoaderBlockLogo.sound));
		}
		if (drawToggleButton(matrices, 70, 5, 90, 20, "Reverse: " + LoaderBlockLogo.rawReverse.name(), mouseX, mouseY, false)){
			int i = LoaderBlockLogo.rawReverse.ordinal()+1;
			if (i>=LoaderBlockLogo.Reverse.values().length) i=0;
			LoaderBlockLogo.rawReverse = LoaderBlockLogo.Reverse.values()[i];
			LoaderBlockLogo.getReverse = LoaderBlockLogo.rawReverse.sup;
			LoaderBlockLogo.set("general.reverse", LoaderBlockLogo.rawReverse.name().toLowerCase(Locale.ROOT));
		}
		textRenderer.draw(matrices, "Shadow Color:", width-160, 2, -1);
		if (filter.length() != 0) {
			textRenderer.draw(matrices, "Filter:", width/2f-20, 2, -1);
			textRenderer.draw(matrices, filter.toString(), width/2f-20, 12, -1);
		}
		if (drawNumSelectable(matrices, width-160, 12, 35, 15, "R: ", LoaderBlockLogo.rawShadowRed, mouseX, mouseY, 1)){
			LoaderBlockLogo.rawShadowRed = num;
			LoaderBlockLogo.shadowRed = num / 255.f;
			num = 0;
			LoaderBlockLogo.set("shadow.red", String.valueOf(LoaderBlockLogo.rawShadowRed));
		}
		if (drawNumSelectable(matrices, width-120, 12, 35, 15, "G: ", LoaderBlockLogo.rawShadowGreen, mouseX, mouseY, 2)){
			LoaderBlockLogo.rawShadowGreen = num;
			LoaderBlockLogo.shadowGreen = num / 255.f;
			num = 0;
			LoaderBlockLogo.set("shadow.green", String.valueOf(LoaderBlockLogo.rawShadowGreen));
		}
		if (drawNumSelectable(matrices, width-80, 12, 35, 15, "B: ", LoaderBlockLogo.rawShadowBlue, mouseX, mouseY, 3)){
			LoaderBlockLogo.rawShadowBlue = num;
			LoaderBlockLogo.shadowBlue = num / 255.f;
			num = 0;
			LoaderBlockLogo.set("shadow.blue", String.valueOf(LoaderBlockLogo.rawShadowBlue));
		}
		if (drawNumSelectable(matrices, width-40, 12, 35, 15, "A: ", LoaderBlockLogo.rawShadowAlpha, mouseX, mouseY, 4)) {
			LoaderBlockLogo.rawShadowAlpha = num;
			LoaderBlockLogo.shadowAlpha = num / 255.f;
			num = 0;
			LoaderBlockLogo.set("shadow.alpha", String.valueOf(LoaderBlockLogo.rawShadowAlpha));
		}
		if (selectedColor == null) {
			float scroll = sidebarHeight < height-startY ? 0 : sidebarScroll;
			scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
			float y = startY+5-scroll;
			for (int clr : LoaderBlockLogo.validColors) {
				if (!(y<startY)) {
					textRenderer.draw(matrices, String.valueOf(clr), 5+0.2F, y+0.2F, clr ^ 0xFFFFFF);
					textRenderer.draw(matrices, String.valueOf(clr), 5, y, clr);
				}
				if (didClick && mouseX >= 0 && mouseX <= width/2 && mouseY > y && mouseY < y+12) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
					selectedColor = clr;
					filter = new StringBuilder();
					if (!LoaderBlockLogo.fullColorToState.containsKey(clr)) LoaderBlockLogo.fullColorToState.put(clr, new ArrayList<>());
				}
				y += 12;
				if (y>height) break;
			}
			sidebarHeight = LoaderBlockLogo.validColors.size()*12+8;
		} else {
			{
				float scroll = sidebarHeight < height - startY ? 0 : sidebarScroll;
				scroll = (float) (Math.floor((scroll * client.getWindow().getScaleFactor())) / client.getWindow().getScaleFactor());
				float y = startY+5-scroll;
				for (Identifier clr : registryBlocks) {
					if (!clr.toString().contains(filter)) continue;
					if (!(y < startY)) {
						textRenderer.drawWithShadow(matrices, clr.toString(), 5, y, -1);
					}
					if (didClick && mouseX >= 0 && mouseX <= width / 2 && mouseY > y && mouseY < y + 12) {
						client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
						List<String> l = LoaderBlockLogo.fullColorToState.get(selectedColor);
						l.add(clr.toString());
						LoaderBlockLogo.colorToState.put(selectedColor, () -> {
							String block = l.get(ThreadLocalRandom.current().nextInt(l.size()));
							try {
								BlockArgumentParser parser = new BlockArgumentParser(new StringReader(block), false);
								parser.parse(false);
								return parser.getBlockState();
							} catch (CommandSyntaxException e) {
								FabLog.warn(block + " is not a valid identifier");
								return Blocks.AIR.getDefaultState();
							}
						});
						LoaderBlockLogo.set("pixels." + String.format("%06x", selectedColor), String.join(" ", l));
					}
					y += 12;
					if (y > height) break;
				}
				sidebarHeight = registryBlocks.stream().filter(i->i.toString().contains(filter)).count()*12+8;
			}
			float scroll = sidebar2Height < height-startY ? 0 : sidebar2Scroll;
			scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
			float y = startY-scroll;
			List<String> blocks = LoaderBlockLogo.fullColorToState.get(selectedColor);
			for (int i = 0; i<blocks.size(); i++) {
				String clr = blocks.get(i);
				if (!(y<startY)) {
					textRenderer.drawWithShadow(matrices, clr, width/2f+5, y, -1);
				}
				if (didRClick && mouseX >= 0 && mouseX > width/2 && mouseY > y && mouseY < y+12) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
					blocks.remove(clr);
					if (blocks.size() < 1) {
						LoaderBlockLogo.colorToState.remove(selectedColor);
						LoaderBlockLogo.fullColorToState.get(selectedColor).clear();
						LoaderBlockLogo.remove("pixels."+String.format("%06x", selectedColor));
					} else {
						LoaderBlockLogo.colorToState.put(selectedColor, () -> {
							String block = blocks.get(ThreadLocalRandom.current().nextInt(blocks.size()));
							try {
								BlockArgumentParser parser = new BlockArgumentParser(new StringReader(block), false);
								parser.parse(false);
								return parser.getBlockState();
							} catch (CommandSyntaxException e) {
								FabLog.warn(block+" is not a valid identifier");
								return Blocks.AIR.getDefaultState();
							}
						});
						LoaderBlockLogo.set("pixels."+String.format("%06x", selectedColor), String.join(" ", blocks));
					}
				}
				y += 12;
				if (y>height) break;
			}
			sidebar2Height = blocks.size()*12+8;
		}
		blockLogo.drawLogo(false, 0, delta);
		if (didClick) didClick = false;
		if (didRClick) didRClick = false;
	}
	private boolean drawNumSelectable(MatrixStack matrices, int x, int y, int w, int h, String text, int val, float mouseX, float mouseY, int index) {
		if (didRClick && mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h) {
			num = 0;
			selected = 0;
			return true;
		}
		if (didClick && selected == index){
			if (num > 255) num = 255;
			if (num < 0) num = 0;
			selected = 0;
			return true;
		}
		if (drawToggleButton(matrices, x, y, w, h, text + (num == 0 || selected != index ? val : num), mouseX, mouseY, selected == index)){
			selected = index;
			num = val;
			return false;
		}
		return false;
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
		blockLogo.tick();

		if (sidebarHeight > height-startY) {
			sidebarScroll += (sidebarScrollTarget-sidebarScroll)/2;
			if (sidebarScrollTarget < 0) sidebarScrollTarget /= 2;
			float h = sidebarHeight-height-startY;
			if (sidebarScrollTarget > h) sidebarScrollTarget = h+((sidebarScrollTarget-h)/2);
		}
		if (sidebar2Height > height-startY) {
			sidebar2Scroll += (sidebar2ScrollTarget-sidebar2Scroll)/2;
			if (sidebar2ScrollTarget < 0) sidebar2ScrollTarget /= 2;
			float h = sidebar2Height-height-startY;
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
			if (LoaderBlockLogo.image.getHeight()+90 > mouseY && mouseY > 40) {
				LoaderBlockLogo.reloadImage();
				startY = LoaderBlockLogo.image.getHeight()+90;
				blockLogo = new BlockLogoRenderer();
			}
			didClick = true;
		} else if (button == 1){
			if (filter.length() > 0 && mouseY < 40 && mouseX > width/2f-40 && mouseY < width/2f+40) {
				filter = new StringBuilder();
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
			}
			if (selectedColor != null && LoaderBlockLogo.image.getHeight()+90 < mouseY && mouseX < width/2d) {
				selectedColor = null;
			}
			didRClick = true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (mouseY>=startY) {
			if (mouseX <= width/2d) {
				sidebarScrollTarget -= amount * 20;
			} else {
				sidebar2ScrollTarget -= amount * 20;
			}
		} else if (mouseY < 40 && mouseX>width-160 && selected != 0){
			num+=amount;
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (selected != 0) {
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE)
				num = 0;
			else if (keyCode>= GLFW.GLFW_KEY_KP_0 && keyCode<=GLFW.GLFW_KEY_KP_9)
				num = num*10 + keyCode - GLFW.GLFW_KEY_KP_0;
			else if (keyCode>=GLFW.GLFW_KEY_0 && keyCode<=GLFW.GLFW_KEY_9)
				num = num*10 + keyCode - GLFW.GLFW_KEY_0;
			return super.keyPressed(keyCode, scanCode, modifiers);
		} else if (selectedColor != null) {
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
				if (hasShiftDown()) filter = new StringBuilder();
				else if (filter.length() > 0) filter.deleteCharAt(filter.length()-1);
			} else {
				String c = GLFW.glfwGetKeyName(keyCode, scanCode);
				if (c != null) filter.append(c);
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
