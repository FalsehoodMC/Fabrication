package com.unascribed.fabrication.mixin.i_woina.dirt_screen;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
@EligibleIf(envMatches=Env.CLIENT)
public class MixinTitleScreen extends Screen {

    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Shadow
    @Final
    boolean doBackgroundFade;

    @Shadow
    long backgroundFadeStart;

    @Redirect(method = "render", at = @At(value="INVOKE", target="Lnet/minecraft/client/gui/RotatingCubeMapRenderer;render(FF)V"))
    public void drawDirt(RotatingCubeMapRenderer rotatingCubeMapRenderer, float delta, float alpha) {
        if (MixinConfigPlugin.isEnabled("*.dirt_screen")) {
            renderBackgroundTexture(0);
        }
        else {
            float f = doBackgroundFade ? (float)(Util.getMeasuringTimeMs() - backgroundFadeStart) / 1000.0F : 1.0F;
            rotatingCubeMapRenderer.render(delta, MathHelper.clamp(f,0.0F, 1.0F));
        }
    }
}
