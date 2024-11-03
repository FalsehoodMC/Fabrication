package com.unascribed.fabrication.mixin.i_woina.dirt_screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

	@Unique
	private static final Identifier DIRT = Identifier.ofVanilla("textures/block/dirt.png");

	protected MixinTitleScreen(Text title) {
		super(title);
	}

	@FabInject(at=@At("HEAD"), method="renderPanoramaBackground(Lnet/minecraft/client/gui/DrawContext;F)V", cancellable=true)
	public void drawDirt(DrawContext context, float delta, CallbackInfo ci) {
		if (FabConf.isEnabled("*.dirt_screen")) {
			// TODO: check if we need to mess with atlases
			RenderSystem.enableBlend();
			context.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
			context.drawTexture(DIRT, 0, 0, 0, 0, 0, this.width, this.height, 16, 16);
			context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.disableBlend();
			ci.cancel();
		}
	}
}
