package com.unascribed.fabrication.client;

import com.google.common.collect.Lists;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.interfaces.GetServerConfig;
import com.unascribed.fabrication.support.ConfigValues;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FabricationSummaryScreen extends Screen {
	PrideFlagRenderer prideFlag;
	final Screen parent;
	final List<String> features;
	final ScrollBar scrollBar = new ScrollBar(height);
	boolean didClick = false;
	double lastMouseX, lastMouseY;

	public FabricationSummaryScreen(Screen parent, List<String> options) {
		super(new LiteralText(MixinConfigPlugin.MOD_NAME+" summary"));
		this.prideFlag = OptionalPrideFlag.get();
		this.parent = parent;
		this.features = options;
	}
	public static FabricationSummaryScreen tryCreate(Screen parent) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.getServer() != null) {
			List<String> features = new ArrayList<>();
			for (String key : FabConf.getAllKeys()) {
				ConfigValues.ResolvedFeature feature = FabConf.getResolvedValue(key, true);
				if (feature != null && feature.value) features.add(key);
			}
			return new FabricationSummaryScreen(parent, features);
		}
		ClientPlayNetworkHandler cpnh = client.getNetworkHandler();
		if (cpnh instanceof GetServerConfig) {
			GetServerConfig gsc = (GetServerConfig) cpnh;
			if (gsc.fabrication$hasHandshook()) {
				List<String> features = new ArrayList<>();
				for (String key : gsc.fabrication$getServerTrileanConfig().keySet()) {
					ConfigValues.ResolvedFeature feature = ((GetServerConfig) client.getNetworkHandler()).fabrication$getServerTrileanConfig().getOrDefault(key, ConfigValues.ResolvedFeature.DEFAULT_FALSE);;
					if (feature != null && feature.value) features.add(key);
				}
				return new FabricationSummaryScreen(parent, features);
			}
		}
		return null;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		FabricationConfigScreen.drawBackground(height, width, client, prideFlag, 0, matrices, 0, 0, 0, 0, 0);
		float scroll = scrollBar.getScaledScroll(client);
		scrollBar.height = -20;
		scroll = (float) (Math.floor((scroll * client.getWindow().getScaleFactor())) / client.getWindow().getScaleFactor());
		float y = 22 - scroll;
		for (String key : features) {
			if (key.startsWith("general.category")) continue;
			FeaturesFile.FeatureEntry entry = FeaturesFile.get(key);
			float h = 0;
			if (entry == null) {
				h = drawWrappedText(matrices, 25, y, "Unknown feature: "+key, width - 100, 0xFFFFFF, false) + 6;
				scrollBar.height += h;
				y+=h;
			} else {
				h = drawWrappedText(matrices, 25, y, entry.name, width - 100, 0xFFFFFF, false) + 6;
				h += drawWrappedText(matrices, 50, y+h, entry.desc, width - 100, 0xFFFFFF, false) + 6;
				scrollBar.height += h;
				y+=h;
			}
		}
	}
	private int drawWrappedText(MatrixStack matrices, float x, float y, String str, int width, int color, boolean fromBottom) {
		int height = 0;
		List<OrderedText> lines = textRenderer.wrapLines(new LiteralText(str), width);
		if (fromBottom) {
			y -= 12;
			lines = Lists.reverse(lines);
		}
		for (OrderedText ot : lines) {
			textRenderer.draw(matrices, ot, x, y, color);
			y += (fromBottom ? -12 : 12);
			height += 12;
		}
		return height;
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
	public void mouseMoved(double x, double y) {
        lastMouseX = x;
        lastMouseY = y;

	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		switch (keyCode) {
			case GLFW.GLFW_KEY_PAGE_UP: mouseScrolled(lastMouseX, lastMouseY, 20); break;
			case GLFW.GLFW_KEY_PAGE_DOWN: mouseScrolled(lastMouseX, lastMouseY, -20); break;
			case GLFW.GLFW_KEY_UP: mouseScrolled(lastMouseX, lastMouseY, 2); break;
			case GLFW.GLFW_KEY_DOWN: mouseScrolled(lastMouseX, lastMouseY, -2); break;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void onClose() {
		client.openScreen(parent);
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
