package com.unascribed.fabrication.logic;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import org.lwjgl.opengl.ARBCopyImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.platform.GlStateManager;
import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.loaders.LoaderClassicBlockDrops;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.client.texture.NativeImage.Format;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3i;

public class WoinaDrops {

	public static float curTimer;
	
	private static AbstractTexture mippedBlocks;
	
	public static boolean mippedBlocksInvalid = true;

	public static int modifyOverlay(ItemStack stack, int overlay) {
		if (MixinConfigPlugin.isEnabled("*.blinking_drops")) {
			return OverlayTexture.getUv(Math.max(0, MathHelper.sin((curTimer+(stack.hashCode()%2000))/5.3f))*0.7f, false);
		}
		return overlay;
	}
	
	public static void interceptRender(ItemRenderer subject, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
		if (MixinConfigPlugin.isEnabled("*.classic_block_drops")) {
			if (stack.getItem() instanceof BlockItem && model instanceof BasicBakedModel && model.hasDepth()) {
				matrices.push();
				model.getTransformation().getTransformation(renderMode).apply(leftHanded, matrices);
				matrices.translate(-0.5, -0.5, -0.5);
				if (LoaderClassicBlockDrops.isSafe(((BlockItem)stack.getItem()).getBlock())) {
					Random r = new Random();
					long seed = 42;
					RenderLayer layer = RenderLayers.getItemLayer(stack, true);
					VertexConsumer vertices = vertexConsumers.getBuffer(layer);
					final int overlayf = overlay;
					for (Direction dir : Direction.values()) {
						r.setSeed(seed);
						model.getQuads(null, dir, r).forEach(q -> drawExaggeratedQuad(stack, matrices, vertices, q, light, overlayf));
					}
					r.setSeed(seed);
					model.getQuads(null, null, r).forEach(q -> drawExaggeratedQuad(stack, matrices, vertices, q, light, overlayf));
				} else {
					if (mippedBlocks == null || mippedBlocksInvalid) {
						mippedBlocksInvalid = false;
						mippedBlocks = new AbstractTexture() {
							
							@Override
							public void load(ResourceManager manager) throws IOException {
								clearGlId();
								SpriteAtlasTexture atlas = MinecraftClient.getInstance().getBakedModelManager().method_24153(new Identifier("textures/atlas/blocks.png"));
								GlStateManager.bindTexture(atlas.getGlId());
								int maxLevel = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL);
								if (maxLevel == 0 || !GL.getCapabilities().GL_ARB_copy_image) {
									int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
									int h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
									ByteBuffer dest = MemoryUtil.memAlloc(w*h*4);
									try {
										GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, Format.ABGR.getPixelDataFormat(), GL11.GL_UNSIGNED_BYTE, MemoryUtil.memAddress(dest));
									} catch (Error | RuntimeException e) {
										MemoryUtil.memFree(dest);
										throw e;
									}
									NativeImage img = FabRefl.Client.NativeImage_new(Format.ABGR, w, h, false, MemoryUtil.memAddress(dest));
									try {
										NativeImage mipped = MipmapHelper.getMipmapLevelsImages(img, 1)[1];
										try {
											TextureUtil.allocate(getGlId(), mipped.getWidth(), mipped.getHeight());
											GlStateManager.bindTexture(getGlId());
											mipped.upload(0, 0, 0, true);
										} finally {
											mipped.close();
										}
									} finally {
										img.close();
									}
								} else {
									int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 1, GL11.GL_TEXTURE_WIDTH);
									int h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 1, GL11.GL_TEXTURE_HEIGHT);
									TextureUtil.allocate(getGlId(), w, h);
									ARBCopyImage.glCopyImageSubData(
											atlas.getGlId(), GL11.GL_TEXTURE_2D, 1, 0, 0, 0,
											getGlId(), GL11.GL_TEXTURE_2D, 0, 0, 0, 0,
											w, h, 1);
								}
							}
						};
						MinecraftClient.getInstance().getTextureManager().registerTexture(new Identifier("fabrication", "textures/atlas/blocks-mip.png"), mippedBlocks);
					}
					RenderLayer defLayer = RenderLayers.getItemLayer(stack, true);
					RenderLayer layer = defLayer == TexturedRenderLayers.getEntityCutout() ?
							RenderLayer.getEntityCutout(new Identifier("fabrication", "textures/atlas/blocks-mip.png")) :
							RenderLayer.getEntityTranslucent(new Identifier("fabrication", "textures/atlas/blocks-mip.png"));
					VertexConsumer vertices = vertexConsumers.getBuffer(layer);
					FabRefl.Client.ItemRenderer_renderBakedItemModel(subject, model, stack, light, overlay, matrices, vertices);
				}
				matrices.pop();
				return;
			}
		}
		subject.renderItem(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
	}
	
	private static void drawExaggeratedQuad(ItemStack is, MatrixStack matrices, VertexConsumer vertices, BakedQuad quad, int light, int overlay) {
		boolean isProbablyGrass = false;
		
		int packedColor = -1;
		if (quad.hasColor()) {
			packedColor = FabRefl.Client.getItemColors(MinecraftClient.getInstance()).getColorMultiplier(is, quad.getColorIndex());
			Block b = ((BlockItem)is.getItem()).getBlock();
			if (b.getDefaultState().getMaterial() == Material.SOIL || b.getDefaultState().getMaterial() == Material.SOLID_ORGANIC) {
				isProbablyGrass = true;
			}
		}
		float tintR = (packedColor >> 16 & 0xFF) / 255.0f;
		float tintG = (packedColor >> 8 & 0xFF) / 255.0f;
		float tintB = (packedColor & 0xFF) / 255.0f;
		
		MatrixStack.Entry ent = matrices.peek();
		int[] data = quad.getVertexData();
		Vec3i faceVec = quad.getFace().getVector();
		Vector3f normal = new Vector3f(faceVec.getX(), faceVec.getY(), faceVec.getZ());
		Vector4f pos = new Vector4f(0, 0, 0, 1);
		Matrix4f mat = ent.getModel();
		normal.transform(ent.getNormal());
		int j = data.length / 8;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			ByteBuffer buf = stack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSize());
			IntBuffer iBuf = buf.asIntBuffer();
			float minU = Float.POSITIVE_INFINITY;
			float maxU = Float.NEGATIVE_INFINITY;
			float minV = Float.POSITIVE_INFINITY;
			float maxV = Float.NEGATIVE_INFINITY;
			for (int pass = 0; pass < 2; pass++) {
				for (int i = 0; i < j; ++i) {
					// IntBuffer-specific clear() override was added after Java 8
					// This cast avoids a NSME
					((Buffer)iBuf).clear();
					iBuf.put(data, i * 8, 8);
					float u = buf.getFloat(16);
					float v = buf.getFloat(20);
					
					if (pass == 0) {
						minU = Math.min(minU, u);
						maxU = Math.max(maxU, u);
						minV = Math.min(minV, v);
						maxV = Math.max(maxV, v);
					} else if (pass == 1) {
						float x = buf.getFloat(0);
						float y = buf.getFloat(4);
						float z = buf.getFloat(8);
						
						float r = ((buf.get(12) & 0xFF) / 255.0f) * tintR;
						float g = ((buf.get(13) & 0xFF) / 255.0f) * tintG;
						float b = ((buf.get(14) & 0xFF) / 255.0f) * tintB;
						
						pos.set(x, y, z, 1);
						pos.transform(mat);
						
						float uSize = maxU-minU;
						float vSize = maxV-minV;

						if (u == minU) {
							u = minU + (uSize*(4/16f));
						} else if (u == maxU) {
							u = minU + (uSize*(12/16f));
						}
						if (v == minV) {
							v = minV + (vSize*((isProbablyGrass ? 1 : 4)/16f));
						} else if (v == maxV) {
							v = minV + (vSize*((isProbablyGrass ? 9 : 12)/16f));
						}
						
						vertices.vertex(pos.getX(), pos.getY(), pos.getZ(),
								r, g, b, 1,
								u, v,
								overlay, light,
								normal.getX(), normal.getY(), normal.getZ());
					}
				}
			}
		}
	}

}
