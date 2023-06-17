package com.unascribed.fabrication.mixin.i_woina.block_logo;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.util.BlockLogoRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
@EligibleIf(configAvailable="*.block_logo", envMatches=Env.CLIENT)
public class MixinTitleScreen extends Screen {

	protected MixinTitleScreen(Text title) {
		super(title);
	}

	private final BlockLogoRenderer fabrication$blockLogo = new BlockLogoRenderer();
	@Shadow
	private SplashTextRenderer splashText;
	private SplashTextRenderer fabrication$splashText;

	@Shadow @Final
	private boolean doBackgroundFade;
	@Shadow
	private long backgroundFadeStart;

	@Hijack(method="render(Lnet/minecraft/client/gui/DrawContext;IIF)V", target="Lnet/minecraft/client/gui/LogoDrawer;draw(Lnet/minecraft/client/gui/DrawContext;IF)V")
	public boolean fabrication$drawBlockLogo() {
		if (FabConf.isEnabled("*.block_logo")) {
			fabrication$blockLogo.drawLogo(doBackgroundFade, backgroundFadeStart, MinecraftClient.getInstance().getTickDelta());
			return true;
		}
		return false;
	}

	@FabInject(at=@At("HEAD"), method="render(Lnet/minecraft/client/gui/DrawContext;IIF)V")
	public void renderHead(DrawContext matrices, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.block_logo")) return;
		fabrication$splashText = splashText;
		splashText = null;
	}

	@FabInject(at=@At("RETURN"), method="render(Lnet/minecraft/client/gui/DrawContext;IIF)V")
	public void renderReturn(DrawContext matrices, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.block_logo")) return;
		splashText = fabrication$splashText;
		fabrication$splashText = null;
	}

	@FabInject(at=@At("TAIL"), method="render(Lnet/minecraft/client/gui/DrawContext;IIF)V")
	public void renderTail(DrawContext drawContext, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.block_logo")) return;
		if (splashText != null) {
			splashText.render(drawContext, width, textRenderer, 1);
		}

	}


	@FabInject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.block_logo")) return;
		fabrication$blockLogo.tick();
	}

}
