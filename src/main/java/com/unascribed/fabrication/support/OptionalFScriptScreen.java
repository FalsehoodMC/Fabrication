package com.unascribed.fabrication.support;

import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.client.FabricationConfigScreen;
import com.unascribed.fabrication.loaders.LoaderFScript;
import io.github.queerbric.pride.PrideFlag;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import tf.ssf.sfort.script.ScriptingScreen;

public class OptionalFScriptScreen extends ScriptingScreen {
    String configKey;
    PrideFlag prideFlag;
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
    }

    @Override
    protected void drawScriptButtons(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int x = this.width - 100;
        if (this.drawButton(matrices, x, this.height - 20, 50, 20, "Save", null, mouseX, mouseY)) {
            OptionalFScript.setScript(configKey, this.unloadScript());
        }
        x -= 50;

        if (this.drawButton(matrices, x, this.height - 20, 50, 20, "Reset", "Restore Fabrications default behaviour", mouseX, mouseY)) {
            OptionalFScript.restoreDefault(configKey);
        }
        x -= 50;

        if (this.drawButton(matrices, x, this.height - 20, 50, 20, "Load", null, mouseX, mouseY)) {
            String script = LoaderFScript.get(configKey);
            if (script != null)this.loadScript(script);
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
