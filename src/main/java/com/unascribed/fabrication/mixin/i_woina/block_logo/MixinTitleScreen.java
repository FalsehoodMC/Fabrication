package com.unascribed.fabrication.mixin.i_woina.block_logo;

import static org.lwjgl.opengl.GL11.*;

import java.util.function.BiConsumer;

import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.LogoBlock;
import com.unascribed.fabrication.loaders.LoaderBlockLogo;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

@Mixin(TitleScreen.class)
@EligibleIf(configEnabled="*.block_logo", envMatches=Env.CLIENT)
public class MixinTitleScreen extends Screen {

	private static final Identifier FABRICATION$EMPTY = new Identifier("fabrication", "empty.png");
	
	@Shadow @Final
	private static Identifier EDITION_TITLE_TEXTURE;
	
	
	protected MixinTitleScreen(Text title) {
		super(title);
	}
	
	private LogoBlock[][] fabrication$blocks = null;
	@Shadow
	private String splashText;
	private String fabrication$splashText;
	
	@Shadow
	private boolean doBackgroundFade;
	@Shadow
	private long backgroundFadeStart;
	
	@Redirect(at=@At(value="INVOKE", target="net/minecraft/client/gui/screen/TitleScreen.method_29343(IILjava/util/function/BiConsumer;)V"),
			method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", expect=2)
	public void drawLogo(TitleScreen subject, int x, int y, BiConsumer<Integer, Integer> callback) {
		if (!MixinConfigPlugin.isEnabled("*.block_logo")) {
			subject.drawWithOutline(x, y, callback);
			return;
		}
		drawLogo(MinecraftClient.getInstance().getTickDelta());
	}
	
	@Redirect(at=@At(value="INVOKE", target="net/minecraft/client/texture/TextureManager.bindTexture(Lnet/minecraft/util/Identifier;)V"),
			method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
	public void bindTexture(TextureManager subject, Identifier id) {
		if (MixinConfigPlugin.isEnabled("*.block_logo") && id == EDITION_TITLE_TEXTURE) {
			id = FABRICATION$EMPTY;
		}
		subject.bindTexture(id);
	}
	
	@Inject(at=@At("HEAD"), method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
	public void renderHead(MatrixStack matrices, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.block_logo")) return;
		fabrication$splashText = splashText;
		splashText = null;
	}
	
	@Inject(at=@At("RETURN"), method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
	public void renderReturn(MatrixStack matrices, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.block_logo")) return;
		splashText = fabrication$splashText;
		fabrication$splashText = null;
	}
	
	@Inject(at=@At("TAIL"), method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V")
	public void renderTail(MatrixStack matrices, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.block_logo")) return;
		if (splashText != null) {
			float fade = doBackgroundFade ? MathHelper.clamp(((Util.getMeasuringTimeMs() - backgroundFadeStart) / 1000f)-1, 0, 1) : 1;
			int l = MathHelper.ceil(fade * 255.0f) << 24;
			RenderSystem.pushMatrix();
			RenderSystem.translatef(this.width / 2 + ((LoaderBlockLogo.unrecoverableLoadError ? 48 : LoaderBlockLogo.image.getWidth())*2.307692307692308f), 70, 0);
			RenderSystem.rotatef(-20, 0, 0, 1);
			float s = 1.8f - MathHelper.abs(MathHelper.sin(Util.getMeasuringTimeMs() % 1000 / 1000f * 6.28f) * 0.1f);
			s = s * 100f / (textRenderer.getWidth(splashText) + 32);
			RenderSystem.scalef(s, s, s);
			drawCenteredText(matrices, textRenderer, splashText, 0, -8, 0xFFFF00 | l);
			RenderSystem.popMatrix();
		}
	}
	
	
	@Inject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (fabrication$blocks != null) {
			for (int y = 0; y < fabrication$blocks.length; y++) {
				for (int x = 0; x < fabrication$blocks[y].length; x++) {
					LogoBlock blk = fabrication$blocks[y][x];
					if (blk != null) {
						blk.tick();
					}
				}
			}
		}
	}
	
	@Unique
	private void drawLogo(float partialTicks) {
		MinecraftClient mc = MinecraftClient.getInstance();
		float fade = doBackgroundFade ? MathHelper.clamp(((Util.getMeasuringTimeMs() - backgroundFadeStart) / 1000f)-1, 0, 1) : 1;
		int logoDataWidth = LoaderBlockLogo.unrecoverableLoadError ? 48 : LoaderBlockLogo.image.getWidth();
		int logoDataHeight = LoaderBlockLogo.unrecoverableLoadError ? 5 : LoaderBlockLogo.image.getHeight();
		if (fabrication$blocks == null || LoaderBlockLogo.invalidated) {
			LoaderBlockLogo.invalidated = false;
			boolean reverse = LoaderBlockLogo.getReverse.getAsBoolean();
			fabrication$blocks = new LogoBlock[logoDataWidth][logoDataHeight];
			if (LoaderBlockLogo.unrecoverableLoadError) {
				String[] error = {
						"### ### ### ### ###    ### ### ###   #   ### ###",
						"#   # # # # # # # #    #   #   #     #   # # #  ",
						"##  ##  ##  # # ##     ### ##  ##    #   # # # #",
						"#   # # # # # # # #      # #   #     #   # # # #",
						"### # # # # ### # # #  ### ### ###   ### ### ###"
				};
				for (int x = 0; x < error[0].length(); x++) {
					for (int y = 0; y < error.length; y++) {
						char c = error[y].charAt(x);
						if (c == ' ') continue;
						BlockState state = null;
						fabrication$blocks[x][y] = new LogoBlock(reverse ? logoDataWidth-x : x, y, state);
					}
				}
			} else {
				NativeImage img = LoaderBlockLogo.image;
				for (int x = 0; x < logoDataWidth; x++) {
					for (int y = 0; y < logoDataHeight; y++) {
						int color = img.getPixelColor(x, y);
						if ((color&0xFF000000) == 0) continue;
						BlockState state = LoaderBlockLogo.colorToState.getOrDefault(color&0x00FFFFFF, () -> Blocks.AIR.getDefaultState()).get();
						if (state.isAir() || state.getRenderType() == BlockRenderType.INVISIBLE) continue;
						fabrication$blocks[x][y] = new LogoBlock(reverse ? logoDataWidth-x : x, y, state);
					}
				}
			}
		}
	
		// ported from beta 1.2_01. hell yeah
		// getting MCP for that version to work was actually pretty easy
		
		GlStateManager.matrixMode(GL_PROJECTION);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		int logoHeight = (int)(120 * mc.getWindow().getScaleFactor());
		GlStateManager.multMatrix(Matrix4f.viewboxMatrix(70, (float)mc.getWindow().getFramebufferWidth()/logoHeight, 0.05f, 100));
		GlStateManager.viewport(0, mc.getWindow().getFramebufferHeight() - logoHeight, mc.getWindow().getFramebufferWidth(), logoHeight);
		GlStateManager.matrixMode(GL_MODELVIEW);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		GlStateManager.disableCull();
		GlStateManager.depthMask(true);
		GlStateManager.pushMatrix();
		DiffuseLighting.enable();
		RenderSystem.setupGui3DDiffuseLighting(
				Util.make(new Vec3f(0f, -1.0f, -0.7f), Vec3f::normalize),
				Util.make(new Vec3f(0f, -1.0f, -0.7f), Vec3f::normalize));
		GlStateManager.popMatrix();
		BlockRenderManager brm = mc.getBlockRenderManager();
		MatrixStack matrices = new MatrixStack();
		for (int pass = 0; pass < 2; pass++) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0.4f, 0.6f, -13);
			if (pass == 0) {
				GlStateManager.clear(GL_DEPTH_BUFFER_BIT, false);
				GlStateManager.translatef(0, -0.4f, 0);
				GlStateManager.scalef(0.98f, 1, 1);
				GlStateManager.enableBlend();
				RenderSystem.defaultBlendFunc();
			}
			if (pass == 1) {
				GlStateManager.disableBlend();
				GlStateManager.clear(GL_DEPTH_BUFFER_BIT, false);
			}
			if (pass == 2) {
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL_SRC_COLOR, GL_ONE);
			}
			GlStateManager.scalef(1, -1, 1);
			GlStateManager.rotatef(15, 1, 0, 0);
			GlStateManager.scalef(0.89f, 1, 0.4f);
			GlStateManager.translatef(-logoDataWidth * 0.5f, -logoDataHeight * 0.5f, 0);
			if (pass == 0) {
				GlStateManager.disableTexture();
				GlStateManager.color4f(1, 1, 1, 1);
			} else {
				GlStateManager.enableTexture();
				GlStateManager.color4f(1, 1, 1, 1);
			}
			
			for (int y = 0; y < logoDataHeight; y++) {
				for (int x = 0; x < logoDataWidth; x++) {
					LogoBlock blk = fabrication$blocks[x][y];
					if (blk == null) continue;
					BlockState state = blk.state;
					GlStateManager.pushMatrix();
					float position = blk.lastPosition + (blk.position - blk.lastPosition) * partialTicks;
					float scale = 1;
					float alpha = 1;
					float rot = 0;
					if (pass == 0) {
						scale = position * 0.04f + 1;
						alpha = 1 / scale;
						position = 0;
					}
					GlStateManager.translatef(x, y, position);
					GlStateManager.scalef(scale, scale, scale);
					GlStateManager.rotatef(rot, 0, 1, 0);
					if (pass != 0) {
						if (state == null) {
							BufferBuilder bb = Tessellator.getInstance().getBuffer();
							bb.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
							Sprite missing = mc.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier("missingno", "missingno"));
							
							float minU = missing.getMinU();
							float minV = missing.getMinV();
							float maxU = missing.getMaxU();
							float maxV = missing.getMaxV();
							
							bb.vertex(0, 0, 0).texture(minU, minV).color(255, 255, 255, 255).normal(0, -1, 0).next();
							bb.vertex(1, 0, 0).texture(maxU, minV).color(255, 255, 255, 255).normal(0, -1, 0).next();
							bb.vertex(1, 0, 1).texture(maxU, maxV).color(255, 255, 255, 255).normal(0, -1, 0).next();
							bb.vertex(0, 0, 1).texture(minU, maxV).color(255, 255, 255, 255).normal(0, -1, 0).next();
							
							bb.vertex(0, 1, 0).texture(minU, minV).color(255, 255, 255, 255).normal(0,  1, 0).next();
							bb.vertex(1, 1, 0).texture(maxU, minV).color(255, 255, 255, 255).normal(0,  1, 0).next();
							bb.vertex(1, 1, 1).texture(maxU, maxV).color(255, 255, 255, 255).normal(0,  1, 0).next();
							bb.vertex(0, 1, 1).texture(minU, maxV).color(255, 255, 255, 255).normal(0,  1, 0).next();
							
							bb.vertex(0, 0, 0).texture(minU, minV).color(255, 255, 255, 255).normal(0, 0, -1).next();
							bb.vertex(1, 0, 0).texture(maxU, minV).color(255, 255, 255, 255).normal(0, 0, -1).next();
							bb.vertex(1, 1, 0).texture(maxU, maxV).color(255, 255, 255, 255).normal(0, 0, -1).next();
							bb.vertex(0, 1, 0).texture(minU, maxV).color(255, 255, 255, 255).normal(0, 0, -1).next();
							
							bb.vertex(0, 0, 1).texture(minU, minV).color(255, 255, 255, 255).normal(0, 0,  1).next();
							bb.vertex(1, 0, 1).texture(maxU, minV).color(255, 255, 255, 255).normal(0, 0,  1).next();
							bb.vertex(1, 1, 1).texture(maxU, maxV).color(255, 255, 255, 255).normal(0, 0,  1).next();
							bb.vertex(0, 1, 1).texture(minU, maxV).color(255, 255, 255, 255).normal(0, 0,  1).next();
							
							bb.vertex(0, 0, 0).texture(minU, minV).color(255, 255, 255, 255).normal(-1, 0, 0).next();
							bb.vertex(0, 1, 0).texture(maxU, minV).color(255, 255, 255, 255).normal(-1, 0, 0).next();
							bb.vertex(0, 1, 1).texture(maxU, maxV).color(255, 255, 255, 255).normal(-1, 0, 0).next();
							bb.vertex(0, 0, 1).texture(minU, maxV).color(255, 255, 255, 255).normal(-1, 0, 0).next();
							
							bb.vertex(1, 0, 0).texture(minU, minV).color(255, 255, 255, 255).normal( 1, 0, 0).next();
							bb.vertex(1, 1, 0).texture(maxU, minV).color(255, 255, 255, 255).normal( 1, 0, 0).next();
							bb.vertex(1, 1, 1).texture(maxU, maxV).color(255, 255, 255, 255).normal( 1, 0, 0).next();
							bb.vertex(1, 0, 1).texture(minU, maxV).color(255, 255, 255, 255).normal( 1, 0, 0).next();
						} else {
							Immediate vertexConsumer = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
							GlStateManager.rotatef(90, 1, 0, 0);
							GlStateManager.translatef(0, 0, -1);
							brm.renderBlockAsEntity(state, matrices, vertexConsumer, 0, OverlayTexture.DEFAULT_UV);
						}
						mc.getTextureManager().bindTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
						Tessellator.getInstance().draw();
					} else {
						GlStateManager.color4f(
								LoaderBlockLogo.shadowRed, LoaderBlockLogo.shadowGreen, LoaderBlockLogo.shadowBlue,
								LoaderBlockLogo.shadowAlpha*alpha*fade);
						BufferBuilder bb = Tessellator.getInstance().getBuffer();
						bb.begin(GL11.GL_QUADS, VertexFormats.POSITION);
						bb.vertex(0, 0, 0).next();
						bb.vertex(1, 0, 0).next();
						bb.vertex(1, 0, 1).next();
						bb.vertex(0, 0, 1).next();
						
						bb.vertex(0, 1, 0).next();
						bb.vertex(1, 1, 0).next();
						bb.vertex(1, 1, 1).next();
						bb.vertex(0, 1, 1).next();
						
						bb.vertex(0, 0, 0).next();
						bb.vertex(1, 0, 0).next();
						bb.vertex(1, 1, 0).next();
						bb.vertex(0, 1, 0).next();
						
						bb.vertex(0, 0, 1).next();
						bb.vertex(1, 0, 1).next();
						bb.vertex(1, 1, 1).next();
						bb.vertex(0, 1, 1).next();
						
						bb.vertex(0, 0, 0).next();
						bb.vertex(0, 1, 0).next();
						bb.vertex(0, 1, 1).next();
						bb.vertex(0, 0, 1).next();
						
						bb.vertex(1, 0, 0).next();
						bb.vertex(1, 1, 0).next();
						bb.vertex(1, 1, 1).next();
						bb.vertex(1, 0, 1).next();
						Tessellator.getInstance().draw();
					}
					GlStateManager.popMatrix();
				}

			}

			GlStateManager.popMatrix();
		}

		DiffuseLighting.disable();
		GlStateManager.disableBlend();
		GlStateManager.matrixMode(GL_PROJECTION);
		GlStateManager.popMatrix();
		GlStateManager.viewport(0, 0, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
		GlStateManager.matrixMode(GL_MODELVIEW);
		GlStateManager.popMatrix();
		GlStateManager.enableCull();
	}
	
}
