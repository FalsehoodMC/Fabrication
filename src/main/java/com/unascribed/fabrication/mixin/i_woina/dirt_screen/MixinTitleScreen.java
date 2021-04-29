package com.unascribed.fabrication.mixin.i_woina.dirt_screen;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
@EligibleIf(configEnabled="*.dirt_screen", envMatches=Env.CLIENT)
public class MixinTitleScreen extends Screen {

    protected MixinTitleScreen(Text title) {
        super(title);
    }

    // @Inject(at=@At(value="INVOKE", target="Lnet/minecraft/client/texture/TextureManager;bindTexture(Lnet/minecraft/util/Identifier;)V"), method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
    @Redirect(method = "render", at = @At(value="INVOKE", target="Lnet/minecraft/client/gui/RotatingCubeMapRenderer;render(FF)V"))
    public void drawDirt(RotatingCubeMapRenderer rotatingCubeMapRenderer, float delta, float alpha) {
        if (MixinConfigPlugin.isEnabled("*.dirt_screen")) {
            renderBackgroundTexture(0);
        }
    }
}
