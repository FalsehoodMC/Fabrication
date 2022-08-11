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
import net.minecraft.client.MinecraftClient;
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
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class BlockLogoScreen extends Screen{
	int selected = 0;
	int num = 0;
	int startY = LoaderBlockLogo.image.getHeight()+90;
	ScrollBar leftBar = new ScrollBar(height-startY);
	ScrollBar rightBar = new ScrollBar(leftBar.displayHeight);

	final Set<Identifier> registryBlocks = Registry.BLOCK.getIds();
	Integer selectedColor = null;
	BlockLogoRenderer blockLogo = new BlockLogoRenderer();
	Pattern filter = Pattern.compile("");
	boolean canFilter = false;
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
			LoaderBlockLogo.instance.set("general.sound", String.valueOf(LoaderBlockLogo.sound));
		}
		if (drawToggleButton(matrices, 70, 5, 90, 20, "Reverse: " + LoaderBlockLogo.rawReverse.name(), mouseX, mouseY, false)){
			int i = LoaderBlockLogo.rawReverse.ordinal()+1;
			if (i>=LoaderBlockLogo.Reverse.values().length) i=0;
			LoaderBlockLogo.rawReverse = LoaderBlockLogo.Reverse.values()[i];
			LoaderBlockLogo.getReverse = LoaderBlockLogo.rawReverse.sup;
			LoaderBlockLogo.instance.set("general.reverse", LoaderBlockLogo.rawReverse.name().toLowerCase(Locale.ROOT));
		}
		textRenderer.draw(matrices, "Shadow Color:", width-160, 2, -1);
		if (filter.pattern().length() > 0) {
			textRenderer.draw(matrices, "Filter:", width/2f-20, 2, -1);
			textRenderer.draw(matrices, filter.toString(), width/2f-20, 12, -1);
		}
		if (drawNumSelectable(matrices, width-160, 12, 35, 15, "R: ", LoaderBlockLogo.rawShadowRed, mouseX, mouseY, 1)){
			LoaderBlockLogo.rawShadowRed = num;
			LoaderBlockLogo.shadowRed = num / 255.f;
			num = 0;
			LoaderBlockLogo.instance.set("shadow.red", String.valueOf(LoaderBlockLogo.rawShadowRed));
		}
		if (drawNumSelectable(matrices, width-120, 12, 35, 15, "G: ", LoaderBlockLogo.rawShadowGreen, mouseX, mouseY, 2)){
			LoaderBlockLogo.rawShadowGreen = num;
			LoaderBlockLogo.shadowGreen = num / 255.f;
			num = 0;
			LoaderBlockLogo.instance.set("shadow.green", String.valueOf(LoaderBlockLogo.rawShadowGreen));
		}
		if (drawNumSelectable(matrices, width-80, 12, 35, 15, "B: ", LoaderBlockLogo.rawShadowBlue, mouseX, mouseY, 3)){
			LoaderBlockLogo.rawShadowBlue = num;
			LoaderBlockLogo.shadowBlue = num / 255.f;
			num = 0;
			LoaderBlockLogo.instance.set("shadow.blue", String.valueOf(LoaderBlockLogo.rawShadowBlue));
		}
		if (drawNumSelectable(matrices, width-40, 12, 35, 15, "A: ", LoaderBlockLogo.rawShadowAlpha, mouseX, mouseY, 4)) {
			LoaderBlockLogo.rawShadowAlpha = num;
			LoaderBlockLogo.shadowAlpha = num / 255.f;
			num = 0;
			LoaderBlockLogo.instance.set("shadow.alpha", String.valueOf(LoaderBlockLogo.rawShadowAlpha));
		}
		if (selectedColor == null) {
			float y = startY+5-leftBar.getScaledScroll(client);
			for (int clr : LoaderBlockLogo.validColors) {
				if (y>=startY) {
					textRenderer.draw(matrices, String.valueOf(clr), 5+0.2F, y+0.2F, clr ^ 0xFFFFFF);
					textRenderer.draw(matrices, String.valueOf(clr), 5, y, clr);
				}
				if (didClick && mouseX >= 0 && mouseX <= width/2 && mouseY > y && mouseY < y+12) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
					selectedColor = clr;
					filter = Pattern.compile("");
					if (!LoaderBlockLogo.fullColorToState.containsKey(clr)) LoaderBlockLogo.fullColorToState.put(clr, new ArrayList<>());
				}
				y += 12;
				if (y>height) break;
			}
			leftBar.height = LoaderBlockLogo.validColors.size()*12+8;
		} else {
			{
				float y = startY+5-leftBar.getScaledScroll(client);
				for (Identifier clr : registryBlocks) {
					if (!filter.matcher(clr.toString()).find()) continue;
					if (y >= startY) {
						textRenderer.drawWithShadow(matrices, clr.toString(), 5, y, -1);
					}
					if (didClick && mouseX >= 0 && mouseX <= width / 2 && mouseY > y && mouseY < y + 12 && y > startY) {
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
						LoaderBlockLogo.instance.set("pixels." + String.format("%06x", selectedColor), String.join(" ", l));
					}
					y += 12;
					if (y > height) break;
				}
				leftBar.height = registryBlocks.stream().filter(i->filter.matcher(i.toString()).find()).count()*12+8;
			}
			float y = startY-rightBar.getScaledScroll(client);
			List<String> blocks = LoaderBlockLogo.fullColorToState.get(selectedColor);
			for (int i = 0; i<blocks.size(); i++) {
				String clr = blocks.get(i);
				if (!(y<startY)) {
					textRenderer.drawWithShadow(matrices, clr, width/2f+5, y, -1);
				}
				if (didRClick && mouseX >= 0 && mouseX > width/2 && mouseY > y && mouseY < y+12 && y > startY) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
					blocks.remove(clr);
					if (blocks.size() < 1) {
						LoaderBlockLogo.colorToState.remove(selectedColor);
						LoaderBlockLogo.fullColorToState.get(selectedColor).clear();
						LoaderBlockLogo.instance.remove("pixels."+String.format("%06x", selectedColor));
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
						LoaderBlockLogo.instance.set("pixels."+String.format("%06x", selectedColor), String.join(" ", blocks));
					}
				}
				y += 12;
				if (y>height) break;
			}
			rightBar.height = blocks.size()*12+8;
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
		return FabricationConfigScreen.drawToggleButton(matrices, x, y, w, h, text, mouseX, mouseY, toggle, didClick, client);
	}

	@Override
	public void tick() {
		super.tick();
		blockLogo.tick();
		leftBar.tick();
		rightBar.tick();
	}

	@Override
	public void renderBackground(MatrixStack matrices) {
		FabricationConfigScreen.drawBackground(height, width, client, prideFlag, 0, matrices, 0, 0, 0, 0, 0);
	}

	@Override
	public void close() {
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
			if (filter.pattern().length() != 0 && mouseY < 40 && mouseX > width/2f-40 && mouseY < width/2f+40) {
				filter = Pattern.compile("");
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
			}
			if (selectedColor != null && LoaderBlockLogo.image.getHeight()+90 < mouseY && mouseX < width/2d) {
				filter = Pattern.compile("");
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
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
				leftBar.scroll(amount * 20);
			} else {
				rightBar.scroll(amount * 20);
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
			if (!canFilter) canFilter = true;
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
				String str = filter.pattern();
				if (hasShiftDown() || str.length() == 1){
					filter = Pattern.compile("");
				} else if (str.length() != 0){
					filter = Pattern.compile(str.substring(0, str.length()-1), Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
				}
			}
		} else {
			if (canFilter) canFilter = false;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (canFilter) {
			filter = Pattern.compile(filter.pattern()+chr, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
		}
		return super.charTyped(chr, modifiers);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		leftBar.displayHeight = height-startY;
		rightBar.displayHeight = leftBar.displayHeight;
		super.resize(client, width, height);
	}

}
