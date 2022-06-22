package com.unascribed.fabrication.mixin.i_woina.block_logo;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.util.BlockLogoRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.loaders.LoaderBlockLogo;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;

@Mixin(TitleScreen.class)
@EligibleIf(configAvailable="*.block_logo", envMatches=Env.CLIENT)
public class MixinTitleScreen extends Screen {

	private static final Identifier FABRICATION$EMPTY = new Identifier("fabrication", "empty.png");

	@Shadow @Final
	private static Identifier EDITION_TITLE_TEXTURE;

	protected MixinTitleScreen(Text title) {
		super(title);
	}

	private final BlockLogoRenderer fabrication$blockLogo = new BlockLogoRenderer();
	@Shadow
	private String splashText;
	private String fabrication$splashText;

	@Shadow @Final
	private boolean doBackgroundFade;
	@Shadow
	private long backgroundFadeStart;

	@Hijack(target="Lnet/minecraft/client/gui/screen/TitleScreen;drawWithOutline(IILjava/util/function/BiConsumer;)V",
			method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
	public boolean fabrication$drawBlockLogo() {
		if (FabConf.isEnabled("*.block_logo")) {
			fabrication$blockLogo.drawLogo(doBackgroundFade, backgroundFadeStart, MinecraftClient.getInstance().getTickDelta());
			return true;
		}
		return false;
	}

	// the mixture of deobf and obf classes here confuses MixinGradle, so we have to spell it out

	@FabModifyArg(at=@At(value="INVOKE", target="com/mojang/blaze3d/systems/RenderSystem.setShaderTexture(ILnet/minecraft/util/Identifier;)V", ordinal=2),
			method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", require=0)
	public Identifier setShaderTextureDev(Identifier id) {
		if (FabConf.isEnabled("*.block_logo") && id == EDITION_TITLE_TEXTURE) {
			id = FABRICATION$EMPTY;
		}
		return id;
	}

	@FabModifyArg(at=@At(value="INVOKE", target="com/mojang/blaze3d/systems/RenderSystem.setShaderTexture(ILnet/minecraft/util/Identifier;)V", ordinal=2),
			method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", require=0)
	public Identifier setShaderTextureObf(Identifier id) {
		if (FabConf.isEnabled("*.block_logo") && id == EDITION_TITLE_TEXTURE) {
			id = FABRICATION$EMPTY;
		}
		return id;
	}

	@FabInject(at=@At("HEAD"), method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
	public void renderHead(MatrixStack matrices, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.block_logo")) return;
		fabrication$splashText = splashText;
		splashText = null;
	}

	@FabInject(at=@At("RETURN"), method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
	public void renderReturn(MatrixStack matrices, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.block_logo")) return;
		splashText = fabrication$splashText;
		fabrication$splashText = null;
	}

	@FabInject(at=@At("TAIL"), method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
	public void renderTail(MatrixStack matrices, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.block_logo")) return;
		if (splashText != null) {
			float fade = doBackgroundFade ? MathHelper.clamp(((Util.getMeasuringTimeMs() - backgroundFadeStart) / 1000f)-1, 0, 1) : 1;
			int l = MathHelper.ceil(fade * 255.0f) << 24;
			matrices.push();
			matrices.translate(this.width / 2.0 + ((LoaderBlockLogo.unrecoverableLoadError ? 48 : LoaderBlockLogo.image.getWidth())*2.307692307692308f), 70, 0);
			matrices.multiply(new Quaternion(0,0,-20, true));
			float s = 1.8f - MathHelper.abs(MathHelper.sin(Util.getMeasuringTimeMs() % 1000 / 1000f * 6.28f) * 0.1f);
			s = s * 100f / (textRenderer.getWidth(splashText) + 32);
			matrices.scale(s, s, s);
			drawCenteredText(matrices, textRenderer, splashText, 0, -8, 0xFFFF00 | l);
			matrices.pop();
		}

	}


	@FabInject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.block_logo")) return;
		fabrication$blockLogo.tick();
	}

}
