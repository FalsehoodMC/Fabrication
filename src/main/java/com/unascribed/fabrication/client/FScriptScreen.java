package com.unascribed.fabrication.client;

import com.mojang.brigadier.tree.CommandNode;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.loaders.LoaderFScript;
import com.unascribed.fabrication.support.OptionalFScript;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.ScriptingScreen;

import java.util.List;
import java.util.stream.Collectors;

public class FScriptScreen extends ScriptingScreen {
	final String fabrication$title;
	String fabrication$key;
	String fabrication$err;
	int fabrication$errBlink = 0;
	PrideFlagRenderer fabrication$prideFlag;
	boolean fabrication$writeLocal = true;
	boolean fabrication$requestedScript = false;

	public FScriptScreen(Screen parent, PrideFlagRenderer prideFlag, String title, String configKey) {
		super(Text.literal("Fabrication Scripting"), parent, new Script(
				"§bFabrication - " + title,
				OptionalFScript.predicateProviders.get(configKey) instanceof Help ? (Help)OptionalFScript.predicateProviders.get(configKey) : default_embed.get(FeaturesFile.get(configKey).fscript),
				null, null, null,
				default_embed
				));
		fabrication$title = "§bFabrication - " + title;
		this.fabrication$prideFlag = prideFlag;
		this.fabrication$key = configKey;
		this.renderTips = true;

	}
	@Override
	public void init(){
		super.init();
		if (this.client !=null && client.player != null) {
			CommandNode<CommandSource> cmd = client.player.networkHandler.getCommandDispatcher().getRoot().getChild("fabrication");
			if (!(cmd == null || cmd.getChild("fscript") == null || cmd.getChild("fscript").getChild("set") == null))
				fabrication$writeLocal = false;
		}
	}
	@Override
	protected void drawOptionButtons(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (drawButton(matrices, width - 83, 1, 35, 10, fabrication$writeLocal ? "Client" : "Server", "Toggle where scripts are saved/loaded", mouseX, mouseY)) {
			if (this.client == null || client.player == null) {
				super.drawOptionButtons(matrices, mouseX, mouseY, delta);
				return;
			}
			CommandNode<CommandSource> cmd = client.player.networkHandler.getCommandDispatcher().getRoot().getChild("fabrication");
			if (!(cmd == null || cmd.getChild("fscript") == null || cmd.getChild("fscript").getChild("set") == null)) {
				fabrication$writeLocal = !fabrication$writeLocal;
			} else {
				fabrication$writeLocal = true;
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.8f, 1));
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.7f, 1));
			}
		}
		super.drawOptionButtons(matrices, mouseX, mouseY, delta);
	}

	@Override
	protected void drawScriptButtons(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int x = this.width - 100;
		if (this.drawButton(matrices, x, this.height - 20, 50, 20, "Save", null, mouseX, mouseY)) {
			if (fabrication$writeLocal){
				OptionalFScript.set(fabrication$key, this.unloadScript(), e -> fabrication$failedSave());
			}else {
				PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
				data.writeVarInt(1);
				data.writeString(fabrication$key);
				data.writeString(this.unloadScript());
				client.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new Identifier("fabrication", "fscript"), data));
			}
		}
		x -= 50;

		if (this.drawButton(matrices, x, this.height - 20, 50, 20, "Reset", "Restore Fabrications default behaviour", mouseX, mouseY)) {
			if (fabrication$writeLocal){
				OptionalFScript.restoreDefault(fabrication$key);
			}else{
				PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
				data.writeVarInt(2);
				data.writeString(fabrication$key);
				client.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new Identifier("fabrication", "fscript"), data));
			}
		}
		x -= 50;

		if (this.drawButton(matrices, x, this.height - 20, 50, 20, "Load", null, mouseX, mouseY)) {
			if (fabrication$writeLocal) {
				String script = LoaderFScript.get(fabrication$key);
				if (script != null){
					this.loadScript(script);
				} else {
					List<String> eq = FabConf.getEquivalent(fabrication$key).stream().map(FeaturesFile::get).filter(f->f.fscriptDefault!=null).map(f->f.name).collect(Collectors.toList());
					if (!eq.isEmpty()) this.client.setScreen(new SelectionScreen(this, eq, this::fabrication$loadDefaultKey));
				}
			}else{
				PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
				data.writeVarInt(0);
				data.writeString(fabrication$key);
				client.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new Identifier("fabrication", "fscript"), data));
				fabrication$requestedScript = true;
			}
		}
	}

	public void fabrication$failedSave(){
		fabrication$err = "Failed To Save Script; Check Logs";
		fabrication$errBlink = 80;
	}
	public void fabrication$loadDefaultKey(Object key) {
		FeaturesFile.FeatureEntry val = FeaturesFile.get(key.toString());
		if (val != null && val.fscriptDefault != null) loadScript(val.fscriptDefault);
	}

	public void fabrication$setScript(String script){
		if(script != null && fabrication$requestedScript){
			this.loadScript(script);
			fabrication$requestedScript = false;
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (fabrication$errBlink > 0 && fabrication$err != null){
			this.script.name = ((fabrication$errBlink & 4) == 0 ? "§c" : "§e")+ fabrication$err;
			fabrication$errBlink--;
			if (fabrication$errBlink == 0) {
				this.script.name = fabrication$title;
			}
		}
	}

	@Override
	protected boolean drawButton(MatrixStack matrices, int x, int y, int w, int h, String text, String desc, int mouseX, int mouseY) {
		fill(matrices, x, y, x+w, y+h, FabConf.isEnabled("general.dark_mode") ? 0x44FFFFFF : 0x55000000);
		return super.drawButton(matrices, x, y, w, h, text, desc, mouseX, mouseY);
	}
	@Override
	protected boolean drawToggleButton(MatrixStack matrices, int x, int y, int w, int h, String text, String desc, int mouseX, int mouseY, boolean toggled) {
		fill(matrices, x, y, x+w, y+h, FabConf.isEnabled("general.dark_mode") ? 0x44FFFFFF : 0x55000000);
		return super.drawToggleButton(matrices, x, y, w, h, text, desc, mouseX, mouseY, toggled);
	}
	@Override
	public void renderBackground(MatrixStack matrices) {
		FabricationConfigScreen.drawBackground(height, width, client, fabrication$prideFlag, 0, matrices, 0, 0, 0, 0, 0);
	}
	@Override
	protected void renderWorldBackground(MatrixStack matrices) {
		renderBackground(matrices);
	}

}
