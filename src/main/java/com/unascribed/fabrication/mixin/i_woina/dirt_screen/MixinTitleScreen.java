package com.unascribed.fabrication.mixin.i_woina.dirt_screen;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
@EligibleIf(envMatches=Env.CLIENT, configEnabled="*.dirt_screen")
public class MixinTitleScreen extends Screen {

	protected MixinTitleScreen(Text title) {
		super(title);
	}

	@Inject(method = "render", at = @At(value="INVOKE", target="Lnet/minecraft/client/gui/RotatingCubeMapRenderer;render(FF)V", shift = At.Shift.AFTER))
	public void drawDirt(CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.dirt_screen")) {
			renderBackgroundTexture(0);
		}
	}
}
