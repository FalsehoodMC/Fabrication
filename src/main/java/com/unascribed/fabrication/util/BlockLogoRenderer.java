package com.unascribed.fabrication.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.loaders.LoaderBlockLogo;
import com.unascribed.fabrication.logic.LogoBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;

public class BlockLogoRenderer {

	private LogoBlock[][] blocks = null;

	public void tick() {
		if (blocks != null) {
			for (LogoBlock[] fabrication$block : blocks) {
				for (LogoBlock blk : fabrication$block) {
					if (blk != null) {
						blk.tick();
					}
				}
			}
		}
	}

	public void drawLogo(boolean doBackgroundFade, long backgroundFadeStart, float partialTicks) {
		MinecraftClient mc = MinecraftClient.getInstance();
		float fade = doBackgroundFade ? MathHelper.clamp(((Util.getMeasuringTimeMs() - backgroundFadeStart) / 1000f)-1, 0, 1) : 1;
		int logoDataWidth = LoaderBlockLogo.unrecoverableLoadError ? 48 : LoaderBlockLogo.image.getWidth();
		int logoDataHeight = LoaderBlockLogo.unrecoverableLoadError ? 5 : LoaderBlockLogo.image.getHeight();
		if (blocks == null || LoaderBlockLogo.invalidated) {
			LoaderBlockLogo.invalidated = false;
			boolean reverse = LoaderBlockLogo.getReverse.getAsBoolean();
			blocks = new LogoBlock[logoDataWidth][logoDataHeight];
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
						blocks[x][y] = new LogoBlock(reverse ? logoDataWidth-x : x, y, state);
					}
				}
			} else {
				NativeImage img = LoaderBlockLogo.image;
				for (int x = 0; x < logoDataWidth; x++) {
					for (int y = 0; y < logoDataHeight; y++) {
						int color = img.getPixelColor(x, y);
						if ((color&0xFF000000) == 0) continue;
						BlockState state = LoaderBlockLogo.colorToState.getOrDefault(color&0x00FFFFFF, Blocks.AIR::getDefaultState).get();
						if (state.isAir() || state.getRenderType() == BlockRenderType.INVISIBLE) continue;
						blocks[x][y] = new LogoBlock(reverse ? logoDataWidth-x : x, y, state);
					}
				}
			}
		}

		// ported from beta 1.2_01. hell yeah
		// getting MCP for that version to work was actually pretty easy
		MatrixStack matrices = new MatrixStack();
		RenderSystem.backupProjectionMatrix();
		int logoHeight = (int)(120 * mc.getWindow().getScaleFactor());
		Matrix4f pmat = Matrix4f.viewboxMatrix(70, mc.getWindow().getFramebufferWidth()/(float)logoHeight, 0.05f, 100);
		RenderSystem.setProjectionMatrix(pmat);
		RenderSystem.viewport(0, mc.getWindow().getFramebufferHeight() - logoHeight, mc.getWindow().getFramebufferWidth(), logoHeight);
		matrices.push();
		matrices.loadIdentity();
		matrices.translate(0.4f, 0.6f, 2000-13);
		RenderSystem.disableCull();
		RenderSystem.depthMask(true);
		RenderSystem.setupLevelDiffuseLighting(
				Util.make(new Vec3f(0f, -1.0f, -0.7f), Vec3f::normalize),
				Util.make(new Vec3f(0f, -1.0f, -0.7f), Vec3f::normalize), matrices.peek().getModel());
		BlockRenderManager brm = mc.getBlockRenderManager();
		BufferBuilder bb = Tessellator.getInstance().getBuffer();
		for (int pass = 0; pass < 2; pass++) {
			matrices.push();
			if (pass == 0) {
				RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);
				matrices.translate(0, -0.4f, 0);
				matrices.scale(0.98f, 1, 1);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
			}
			if (pass == 1) {
				RenderSystem.disableBlend();
				RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);
			}
			if (pass == 2) {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
			}
			matrices.scale(1, -1, 1);
			matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(15));
			matrices.scale(0.89f, 1, 0.4f);
			matrices.translate(-logoDataWidth * 0.5f, -logoDataHeight * 0.5f, 0);

			//			RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
			//			RenderSystem.setShaderColor(1, 1, 1, 1);
			//			RenderSystem.lineWidth(8);
			//			bb.begin(DrawMode.LINES, VertexFormats.LINES);
			//			bb.vertex(matrices.peek().getModel(), -400, 0, 0).color(1, 0, 0, 1f).normal(1, 0, 0).next();
			//			bb.vertex(matrices.peek().getModel(), 400, 0, 0).color(1, 0, 0, 1f).normal(1, 0, 0).next();
			//			bb.vertex(matrices.peek().getModel(), 0, -400, 0).color(0, 1, 0, 1f).normal(0, 1, 0).next();
			//			bb.vertex(matrices.peek().getModel(), 0, 400, 0).color(0, 1, 0, 1f).normal(0, 1, 0).next();
			//			bb.vertex(matrices.peek().getModel(), 0, 0, -400).color(0, 0, 1, 1f).normal(0, 0, 1).next();
			//			bb.vertex(matrices.peek().getModel(), 0, 0, 400).color(0, 0, 1, 1f).normal(0, 0, 1).next();
			//			Tessellator.getInstance().draw();

			if (pass == 0) {
				RenderSystem.disableTexture();
				RenderSystem.setShaderColor(1, 1, 1, 1);
			} else {
				RenderSystem.enableTexture();
				RenderSystem.setShaderColor(1, 1, 1, 1);
			}

			for (int y = 0; y < logoDataHeight; y++) {
				for (int x = 0; x < logoDataWidth; x++) {
					LogoBlock blk = blocks[x][y];
					if (blk == null) continue;
					BlockState state = blk.state;
					matrices.push();
					float position = blk.lastPosition + (blk.position - blk.lastPosition) * partialTicks;
					float scale = 1;
					float alpha = 1;
					float rot = 0;
					if (pass == 0) {
						scale = position * 0.04f + 1;
						alpha = 1 / scale;
						position = 0;
					}
					matrices.translate(x, y, position);
					matrices.scale(scale, scale, scale);
					matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rot));
					if (pass != 0) {
						mc.getTextureManager().bindTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
						RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
						RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
						RenderSystem.setShaderColor(1, 1, 1, 1);
						if (state == null) {
							bb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
							Sprite missing = mc.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier("missingno", "missingno"));

							float minU = missing.getMinU();
							float minV = missing.getMinV();
							float maxU = missing.getMaxU();
							float maxV = missing.getMaxV();

							Matrix4f mat = matrices.peek().getModel();
							bb.vertex(mat, 0, 0, 0).texture(minU, minV).color(255, 255, 255, 255).normal(0, -1, 0).next();
							bb.vertex(mat, 1, 0, 0).texture(maxU, minV).color(255, 255, 255, 255).normal(0, -1, 0).next();
							bb.vertex(mat, 1, 0, 1).texture(maxU, maxV).color(255, 255, 255, 255).normal(0, -1, 0).next();
							bb.vertex(mat, 0, 0, 1).texture(minU, maxV).color(255, 255, 255, 255).normal(0, -1, 0).next();

							bb.vertex(mat, 0, 1, 0).texture(minU, minV).color(255, 255, 255, 255).normal(0,  1, 0).next();
							bb.vertex(mat, 1, 1, 0).texture(maxU, minV).color(255, 255, 255, 255).normal(0,  1, 0).next();
							bb.vertex(mat, 1, 1, 1).texture(maxU, maxV).color(255, 255, 255, 255).normal(0,  1, 0).next();
							bb.vertex(mat, 0, 1, 1).texture(minU, maxV).color(255, 255, 255, 255).normal(0,  1, 0).next();

							bb.vertex(mat, 0, 0, 0).texture(minU, minV).color(255, 255, 255, 255).normal(0, 0, -1).next();
							bb.vertex(mat, 1, 0, 0).texture(maxU, minV).color(255, 255, 255, 255).normal(0, 0, -1).next();
							bb.vertex(mat, 1, 1, 0).texture(maxU, maxV).color(255, 255, 255, 255).normal(0, 0, -1).next();
							bb.vertex(mat, 0, 1, 0).texture(minU, maxV).color(255, 255, 255, 255).normal(0, 0, -1).next();

							bb.vertex(mat, 0, 0, 1).texture(minU, minV).color(255, 255, 255, 255).normal(0, 0,  1).next();
							bb.vertex(mat, 1, 0, 1).texture(maxU, minV).color(255, 255, 255, 255).normal(0, 0,  1).next();
							bb.vertex(mat, 1, 1, 1).texture(maxU, maxV).color(255, 255, 255, 255).normal(0, 0,  1).next();
							bb.vertex(mat, 0, 1, 1).texture(minU, maxV).color(255, 255, 255, 255).normal(0, 0,  1).next();

							bb.vertex(mat, 0, 0, 0).texture(minU, minV).color(255, 255, 255, 255).normal(-1, 0, 0).next();
							bb.vertex(mat, 0, 1, 0).texture(maxU, minV).color(255, 255, 255, 255).normal(-1, 0, 0).next();
							bb.vertex(mat, 0, 1, 1).texture(maxU, maxV).color(255, 255, 255, 255).normal(-1, 0, 0).next();
							bb.vertex(mat, 0, 0, 1).texture(minU, maxV).color(255, 255, 255, 255).normal(-1, 0, 0).next();

							bb.vertex(mat, 1, 0, 0).texture(minU, minV).color(255, 255, 255, 255).normal( 1, 0, 0).next();
							bb.vertex(mat, 1, 1, 0).texture(maxU, minV).color(255, 255, 255, 255).normal( 1, 0, 0).next();
							bb.vertex(mat, 1, 1, 1).texture(maxU, maxV).color(255, 255, 255, 255).normal( 1, 0, 0).next();
							bb.vertex(mat, 1, 0, 1).texture(minU, maxV).color(255, 255, 255, 255).normal( 1, 0, 0).next();
							Tessellator.getInstance().draw();
						} else {
							VertexConsumerProvider.Immediate vertexConsumer = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
							matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
							matrices.translate(0, 0, -1);
							brm.renderBlockAsEntity(state, matrices, vertexConsumer, LightmapTextureManager.pack(15, 15), OverlayTexture.DEFAULT_UV);
							vertexConsumer.draw();
						}
					} else {
						RenderSystem.setShader(GameRenderer::getPositionShader);
						RenderSystem.setShaderColor(
								LoaderBlockLogo.shadowRed, LoaderBlockLogo.shadowGreen, LoaderBlockLogo.shadowBlue,
								LoaderBlockLogo.shadowAlpha*alpha*fade);
						bb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
						Matrix4f mat = matrices.peek().getModel();
						bb.vertex(mat, 0, 0, 0).next();
						bb.vertex(mat, 1, 0, 0).next();
						bb.vertex(mat, 1, 0, 1).next();
						bb.vertex(mat, 0, 0, 1).next();

						bb.vertex(mat, 0, 1, 0).next();
						bb.vertex(mat, 1, 1, 0).next();
						bb.vertex(mat, 1, 1, 1).next();
						bb.vertex(mat, 0, 1, 1).next();

						bb.vertex(mat, 0, 0, 0).next();
						bb.vertex(mat, 1, 0, 0).next();
						bb.vertex(mat, 1, 1, 0).next();
						bb.vertex(mat, 0, 1, 0).next();

						bb.vertex(mat, 0, 0, 1).next();
						bb.vertex(mat, 1, 0, 1).next();
						bb.vertex(mat, 1, 1, 1).next();
						bb.vertex(mat, 0, 1, 1).next();

						bb.vertex(mat, 0, 0, 0).next();
						bb.vertex(mat, 0, 1, 0).next();
						bb.vertex(mat, 0, 1, 1).next();
						bb.vertex(mat, 0, 0, 1).next();

						bb.vertex(mat, 1, 0, 0).next();
						bb.vertex(mat, 1, 1, 0).next();
						bb.vertex(mat, 1, 1, 1).next();
						bb.vertex(mat, 1, 0, 1).next();
						Tessellator.getInstance().draw();
					}
					matrices.pop();
				}

			}

			matrices.pop();
		}

		DiffuseLighting.disableGuiDepthLighting();
		RenderSystem.lineWidth(1);
		RenderSystem.disableBlend();
		RenderSystem.restoreProjectionMatrix();
		RenderSystem.viewport(0, 0, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
		matrices.pop();
		RenderSystem.enableCull();
	}
}
