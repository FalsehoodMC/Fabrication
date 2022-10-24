package com.unascribed.fabrication.mixin.i_woina.dirt_screen;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

@Mixin(TitleScreen.class)
@EligibleIf(envMatches=Env.CLIENT, configAvailable="*.dirt_screen")
public class MixinTitleScreen extends Screen {

	protected MixinTitleScreen(Text title) {
		super(title);
	}

	@FabInject(method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/RotatingCubeMapRenderer;render(FF)V", shift=At.Shift.AFTER))
	public void drawDirt(CallbackInfo ci) {
		if (FabConf.isEnabled("*.dirt_screen")) {
			renderBackgroundTexture(0);
		}
	}
}
