package com.unascribed.fabrication.client;

import com.mojang.brigadier.tree.CommandNode;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.loaders.LoaderFScript;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.OptionalFScript;
import io.github.queerbric.pride.PrideFlag;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import tf.ssf.sfort.script.ScriptingScreen;

public class OptionalFScriptScreen extends ScriptingScreen {
    String configKey;
    PrideFlag prideFlag;
    boolean writeLocal = true;
    boolean requestedScript = false;
    public OptionalFScriptScreen(Screen parent, PrideFlag prideFlag, String title, String configKey) {
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
        if (drawButton(matrices, width-83, 1, 35, 10, writeLocal? "Client":"Server", "Toggle where scripts are saved/loaded", mouseX, mouseY)){
            if (this.client !=null && client.player != null) {
                CommandNode<CommandSource> cmd = client.player.networkHandler.getCommandDispatcher().getRoot().getChild("fabrication");
                if (!(cmd == null || cmd.getChild("fscript") == null || cmd.getChild("fscript").getChild("set") == null))
                    writeLocal = !writeLocal;
                else writeLocal = true;
            }else writeLocal = true;
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
                if (script != null) this.loadScript(script);
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
}
