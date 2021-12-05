package com.unascribed.fabrication.client;

import com.mojang.brigadier.tree.CommandNode;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.loaders.LoaderFScript;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.OptionalFScript;
import io.github.queerbric.pride.PrideFlag;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import tf.ssf.sfort.script.ScriptingScreen;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FScriptScreen extends ScriptingScreen {
	String configKey;
	PrideFlag prideFlag;
	boolean writeLocal = true;
	boolean requestedScript = false;

	public FScriptScreen(Screen parent, PrideFlag prideFlag, String title, String configKey) {
		super(new LiteralText("Fabrication Scripting"), parent, new Script(
				"§bFabrication - " + title,
				default_embed.get(FeaturesFile.get(configKey).fscript),
				null, null, null,
				default_embed
				));
		this.prideFlag = prideFlag;
		this.configKey = configKey;
		this.renderTips = true;
		if (this.client !=null && client.player != null) {
			CommandNode<CommandSource> cmd = client.player.networkHandler.getCommandDispatcher().getRoot().getChild("fabrication");
			if (!(cmd == null || cmd.getChild("fscript") == null || cmd.getChild("fscript").getChild("set") == null))
				writeLocal = false;
		}
	}

	@Override
	protected void drawOptionButtons(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (drawButton(matrices, width - 83, 1, 35, 10, writeLocal ? "Client" : "Server", "Toggle where scripts are saved/loaded", mouseX, mouseY)) {
			if (this.client == null || client.player == null) {
				super.drawOptionButtons(matrices, mouseX, mouseY, delta);
				return;
			}
			CommandNode<CommandSource> cmd = client.player.networkHandler.getCommandDispatcher().getRoot().getChild("fabrication");
			if (!(cmd == null || cmd.getChild("fscript") == null || cmd.getChild("fscript").getChild("set") == null)) {
				writeLocal = !writeLocal;
			} else {
				writeLocal = true;
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.8f, 1));
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.7f, 1));
			}
		}
		super.drawOptionButtons(matrices, mouseX, mouseY, delta);
	}

	@Override
	protected void drawScriptButtons(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int x = this.width - 100;
		if (this.drawButton(matrices, x, this.height - 20, 50, 20, "Save", null, mouseX, mouseY)) {
			if (writeLocal){
				OptionalFScript.set(configKey, this.unloadScript());
			}else {
				PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
				data.writeVarInt(1);
				data.writeString(configKey);
				data.writeString(this.unloadScript());
				client.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new Identifier("fabrication", "fscript"), data));
			}
		}
		x -= 50;

		if (this.drawButton(matrices, x, this.height - 20, 50, 20, "Reset", "Restore Fabrications default behaviour", mouseX, mouseY)) {
			if (writeLocal){
				OptionalFScript.restoreDefault(configKey);
			}else{
				PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
				data.writeVarInt(2);
				data.writeString(configKey);
				client.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new Identifier("fabrication", "fscript"), data));
			}
		}
		x -= 50;

		if (this.drawButton(matrices, x, this.height - 20, 50, 20, "Load", null, mouseX, mouseY)) {
			if (writeLocal) {
				String script = LoaderFScript.get(configKey);
				if (script != null){
					this.loadScript(script);
				} else {
					Set<FeaturesFile.FeatureEntry> eq = MixinConfigPlugin.getEquivalent(configKey).stream().map(FeaturesFile::get).filter(f->f.fscriptDefault!=null).collect(Collectors.toSet());
					if (!eq.isEmpty()) this.client.openScreen(new SelectScreen(this, eq, this::loadScript));
				}
			}else{
				PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
				data.writeVarInt(0);
				data.writeString(configKey);
				client.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new Identifier("fabrication", "fscript"), data));
				requestedScript = true;
			}
		}
	}

	public void fabrication$setScript(String script){
		if(script != null && requestedScript){
			this.loadScript(script);
			requestedScript = false;
		}
	}

	@Override
	protected boolean drawButton(MatrixStack matrices, int x, int y, int w, int h, String text, String desc, int mouseX, int mouseY) {
		fill(matrices, x, y, x+w, y+h, MixinConfigPlugin.isEnabled("general.dark_mode") ? 0x44FFFFFF : 0x55000000);
		return super.drawButton(matrices, x, y, w, h, text, desc, mouseX, mouseY);
	}
	@Override
	protected boolean drawToggleButton(MatrixStack matrices, int x, int y, int w, int h, String text, String desc, int mouseX, int mouseY, boolean toggled) {
		fill(matrices, x, y, x+w, y+h, MixinConfigPlugin.isEnabled("general.dark_mode") ? 0x44FFFFFF : 0x55000000);
		return super.drawToggleButton(matrices, x, y, w, h, text, desc, mouseX, mouseY, toggled);
	}
	@Override
	public void renderBackground(MatrixStack matrices) {
		FabricationConfigScreen.drawBackground(height, width, client, prideFlag, 0, matrices, 0, 0, 0, 0, 0);
	}
	@Override
	protected void renderWorldBackground(MatrixStack matrices) {
		renderBackground(matrices);
	}

	private static class SelectScreen extends Screen {
		final FScriptScreen parent;
		final Set<FeaturesFile.FeatureEntry> features;
		final Consumer<String> out;
		float sidebarScrollTarget;
		float sidebarScroll;
		float sidebarHeight;
		boolean didClick = false;

		protected SelectScreen(FScriptScreen parent, Set<FeaturesFile.FeatureEntry> options, Consumer<String> out) {
			super(parent.title);
			this.out = out;
			this.parent = parent;
			this.features = options;
		}
		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			parent.renderBackground(matrices);
			float scroll = sidebarHeight < height ? 0 : sidebarScroll;
			sidebarHeight = 20;
			scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
			float y = 22-scroll;
			for (FeaturesFile.FeatureEntry feature : features) {
				textRenderer.drawWithShadow(matrices, feature.name, 16, y, -1);
				if (mouseY > y-2 && mouseY < y+10) {
					fill(matrices, 0, (int) y+11, textRenderer.getWidth(feature.name)+16, (int) y+12, -1);
					if (didClick){
						out.accept(feature.fscriptDefault);
						onClose();
					}
				}
				sidebarHeight+=20;
				y+=20;
			}

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
		public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
			sidebarScrollTarget -= amount*20;
			return super.mouseScrolled(mouseX, mouseY, amount);
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

	}
}
