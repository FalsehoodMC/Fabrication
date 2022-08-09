package com.unascribed.fabrication.mixin.i_woina.end_portal_parallax;

import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

@Mixin(EndPortalBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.end_portal_parallax", envMatches=Env.CLIENT, specialConditions=SpecialEligibility.NOT_MACOS)
public abstract class MixinEndPortalRenderer {

	@Shadow
	@Final
	public static Identifier PORTAL_TEXTURE;
	@Shadow
	@Final
	public static Identifier SKY_TEXTURE;
	@Unique
	private static final Random fabrication$random = new Random(31100L);
	private static final FloatBuffer fabrication$matrix_buffer = BufferUtils.createFloatBuffer(4*4);

	@FabInject(at=@At("HEAD"), method= "renderSide(Lnet/minecraft/block/entity/EndPortalBlockEntity;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFLnet/minecraft/util/math/Direction;)V", cancellable=true)
	public void oldRender(EndPortalBlockEntity be, Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, Direction side, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.end_portal_parallax")) return;
		ci.cancel();
		if (!be.shouldDrawSide(side)) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer viewportBuffer = stack.mallocInt(4);
			FloatBuffer scratchBuffer = stack.mallocFloat(16);
			FloatBuffer objectCoords = stack.mallocFloat(3);
			FloatBuffer projBuffer = stack.mallocFloat(16);

			GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewportBuffer);
			Matrix4f scratchMat = RenderSystem.getModelViewMatrix().copy();
			Matrix4f projMatrix = RenderSystem.getProjectionMatrix().copy();
			scratchMat.multiply(model);
			projMatrix.writeRowMajor(projBuffer);
			scratchMat.writeRowMajor(scratchBuffer);

			float objectX = objectCoords.get(0);
			float objectY = objectCoords.get(1);
			float objectZ = objectCoords.get(2);

			BlockPos pos = be.getPos();
			float baseTexTransX = pos.getX();
			float baseTexTransY = pos.getY();
			float baseTexTransZ = pos.getZ();

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			RenderSystem.getProjectionMatrix().writeColumnMajor(fabrication$matrix_buffer);
			fabrication$matrix_buffer.rewind();
			GL11.glMultMatrixf(fabrication$matrix_buffer);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			RenderSystem.getModelViewMatrix().writeColumnMajor(fabrication$matrix_buffer);
			fabrication$matrix_buffer.rewind();
			GL11.glMultMatrixf(fabrication$matrix_buffer);
			model.writeColumnMajor(fabrication$matrix_buffer);
			fabrication$matrix_buffer.rewind();
			GL11.glMultMatrixf(fabrication$matrix_buffer);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDisable(GL11.GL_LIGHTING);
			fabrication$random.setSeed(31100L);
			float yOffset = 0.75F;

			for (int i = 0; i < 16; ++i) {
				GL11.glPushMatrix();
				float ri = 16 - i;
				float scale = 0.0625F;
				float brightness = 1.0F / (ri + 1.0F);
				if (i == 0) {
					mc.getTextureManager().bindTexture(SKY_TEXTURE);
					brightness = 0.1F;
					ri = 65.0F;
					scale = 0.125F;
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				}

				if (i == 1) {
					mc.getTextureManager().bindTexture(PORTAL_TEXTURE);
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
					scale = 0.5F;
				}

				float surfaceY = -yOffset;
				float projRelSurf = surfaceY + objectY;
				float layerY = surfaceY + ri + objectY;
				float texTransY = projRelSurf / layerY;
				texTransY += yOffset;
				GL11.glTranslatef(baseTexTransX, texTransY, baseTexTransZ);
				GL11.glTexGeni(GL11.GL_S, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_OBJECT_LINEAR);
				GL11.glTexGeni(GL11.GL_T, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_OBJECT_LINEAR);
				GL11.glTexGeni(GL11.GL_R, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_OBJECT_LINEAR);
				GL11.glTexGeni(GL11.GL_Q, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);
				GL11.glTexGenfv(GL11.GL_S, GL11.GL_OBJECT_PLANE, new float[]{1.0F, 0.0F, 0.0F, 0.0F});
				GL11.glTexGenfv(GL11.GL_T, GL11.GL_OBJECT_PLANE, new float[]{0.0F, 0.0F, 1.0F, 0.0F});
				GL11.glTexGenfv(GL11.GL_R, GL11.GL_OBJECT_PLANE, new float[]{0.0F, 0.0F, 0.0F, 1.0F});
				GL11.glTexGenfv(GL11.GL_Q, GL11.GL_EYE_PLANE, new float[]{0.0F, 1.0F, 0.0F, 0.0F});
				GL11.glEnable(GL11.GL_TEXTURE_GEN_S);
				GL11.glEnable(GL11.GL_TEXTURE_GEN_T);
				GL11.glEnable(GL11.GL_TEXTURE_GEN_R);
				GL11.glEnable(GL11.GL_TEXTURE_GEN_Q);
				GL11.glPopMatrix();
				GL11.glMatrixMode(GL11.GL_TEXTURE);
				GL11.glPushMatrix();
				GL11.glLoadIdentity();
				GL11.glTranslatef(0.0F, Util.getEpochTimeMs() % 700000L / 700000.0F, 0.0F);
				GL11.glScalef(scale, scale, scale);
				GL11.glTranslatef(0.5F, 0.5F, 0.0F);
				GL11.glRotatef((i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslatef(-0.5F, -0.5F, 0.0F);
				GL11.glTranslatef(-baseTexTransX, -baseTexTransZ, -baseTexTransY);
				projRelSurf = surfaceY + objectY;
				GL11.glTranslatef(objectX * ri / projRelSurf, objectZ * ri / projRelSurf, -baseTexTransY);
				float r = fabrication$random.nextFloat() * 0.5F + 0.1F;
				float g = fabrication$random.nextFloat() * 0.5F + 0.4F;
				float b = fabrication$random.nextFloat() * 0.5F + 0.5F;
				if (i == 0) {
					b = 1.0F;
					g = 1.0F;
					r = 1.0F;
				}

				GL11.glColor4f(r * brightness, g * brightness, b * brightness, 1.0F);
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glVertex3d(x1, y1, z1);
				GL11.glVertex3d(x2, y1, z2);
				GL11.glVertex3d(x2, y2, z3);
				GL11.glVertex3d(x1, y2, z4);
				GL11.glEnd();
				GL11.glPopMatrix();
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
			}

			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_TEXTURE_GEN_S);
			GL11.glDisable(GL11.GL_TEXTURE_GEN_T);
			GL11.glDisable(GL11.GL_TEXTURE_GEN_R);
			GL11.glDisable(GL11.GL_TEXTURE_GEN_Q);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();
		}
	}
}
