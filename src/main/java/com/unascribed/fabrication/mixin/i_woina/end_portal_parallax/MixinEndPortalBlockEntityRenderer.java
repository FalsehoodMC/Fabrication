package com.unascribed.fabrication.mixin.i_woina.end_portal_parallax;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.client.GLUPort;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.injection.FabInject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.EndGatewayBlockEntity;
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

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_EYE_LINEAR;
import static org.lwjgl.opengl.GL11.GL_EYE_PLANE;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX;
import static org.lwjgl.opengl.GL11.GL_OBJECT_LINEAR;
import static org.lwjgl.opengl.GL11.GL_OBJECT_PLANE;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_PROJECTION_MATRIX;
import static org.lwjgl.opengl.GL11.GL_Q;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_R;
import static org.lwjgl.opengl.GL11.GL_S;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_T;
import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_GEN_MODE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_GEN_Q;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_GEN_R;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_GEN_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_GEN_T;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGetFloatv;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glMultMatrixf;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexGenfv;
import static org.lwjgl.opengl.GL11.glTexGeni;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3d;

@Environment(EnvType.CLIENT)
@Mixin(EndPortalBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.end_portal_parallax")
@FailOn(invertedSpecialConditions={SpecialEligibility.NOT_MACOS, SpecialEligibility.NEVER}, modLoaded="endlessencore")
public abstract class MixinEndPortalBlockEntityRenderer {

	@Shadow
	@Final
	public static Identifier PORTAL_TEXTURE;
	@Shadow
	@Final
	public static Identifier SKY_TEXTURE;

	@Unique
	private static final Random RANDOM = new Random(31100L);

	private static final FloatBuffer fabrication$endParalax$MATRIX_BUFFER = BufferUtils.createFloatBuffer(4*4);

	@FabInject(at=@At("HEAD"), method="method_23085(Lnet/minecraft/block/entity/EndPortalBlockEntity;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFFFFLnet/minecraft/util/math/Direction;)V", cancellable=true)
	public void fabrication$render(EndPortalBlockEntity be, Matrix4f model, VertexConsumer vertexConsumer, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, float n, float o, float p, Direction side, CallbackInfo ci) {
		if (FabConf.isEnabled("*.end_portal_parallax") && be.shouldDrawSide(side)) {
			// YOU try to make it work with the gateway, I DARE YOU
			if (be instanceof EndGatewayBlockEntity) return;
			if (side.getHorizontal() != -1) return;
			ci.cancel();
			// ABANDON  ALL HOPE
			// YE WHO ENTER HERE
			MinecraftClient mc = MinecraftClient.getInstance();
			try (MemoryStack stack = MemoryStack.stackPush()) {
				BlockPos pos = be.getPos();

				IntBuffer viewportBuffer = stack.mallocInt(4);
				FloatBuffer scratchBuffer = stack.mallocFloat(16);
				FloatBuffer crosshairsPos = stack.mallocFloat(3);
				FloatBuffer projBuffer = stack.mallocFloat(16);

				glGetIntegerv(GL_VIEWPORT, viewportBuffer);
				float viewportCX = (viewportBuffer.get(0) + viewportBuffer.get(2)) / 2f;
				float viewportCY = (viewportBuffer.get(1) + viewportBuffer.get(3)) / 2f;
				glGetFloatv(GL_PROJECTION_MATRIX, projBuffer);
				glGetFloatv(GL_MODELVIEW_MATRIX, scratchBuffer);
				//projBuffer.rewind();
				//scratchBuffer.rewind();
				/*{
				GlStateManager.getFloat();
				Matrix4f scratchMat = new Matrix4f();
				scratchMat.;
				scratchMat.writeRowFirst();
				Matrix4f projMatrix = RenderSystem.getProjectionMatrix().copy();
				scratchMat.multiply(model);
				projMatrix.writeRowFirst(projBuffer);
				scratchMat.writeRowFirst(scratchBuffer);
				}*/

				GLUPort.gluUnProject(
						viewportCX,
						viewportCY,
						0,
						scratchBuffer,
						projBuffer,
						viewportBuffer,
						crosshairsPos);

				float crosshairsX = crosshairsPos.get(0);
				float crosshairsY = crosshairsPos.get(1);
				float crosshairsZ = crosshairsPos.get(2);

				float baseTexTransX = pos.getX();
				float baseTexTransY = pos.getY();
				float baseTexTransZ = pos.getZ();
				/*glPushMCMatrix*/{
					glMatrixMode(GL_PROJECTION);
					glPushMatrix();
					glLoadIdentity();
					glMultMatrixf(projBuffer);
					glMatrixMode(GL_MODELVIEW);
					glPushMatrix();
					glMultMatrixf(scratchBuffer);
				}
				/*glMultMatrixf*/{
					model.writeRowFirst(fabrication$endParalax$MATRIX_BUFFER);
					fabrication$endParalax$MATRIX_BUFFER.rewind();
					glMultMatrixf(fabrication$endParalax$MATRIX_BUFFER);
				}
				glEnable(GL_TEXTURE_2D);
				glEnable(GL_DEPTH_TEST);
				glDepthFunc(GL_LEQUAL);
				glDisable(GL_LIGHTING);
				RANDOM.setSeed(31100L);

				for (int i = 0; i < 16; ++i) {
					glPushMatrix();
					float ri = 16 - i;
					float scale = 0.0625F;
					float brightness = 1.0F / (ri + 1.0F);
					if (i == 0) {
						mc.getTextureManager().bindTexture(SKY_TEXTURE);
						brightness = 0.1F;
						ri = 65.0F;
						scale = 0.125F;
						glEnable(GL_BLEND);
						/*glDefaultBlendFunc*/ {
							glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
						}
					}

					if (i == 1) {
						mc.getTextureManager().bindTexture(PORTAL_TEXTURE);
						glEnable(GL_BLEND);
						glBlendFunc(GL_ONE, GL_ONE);
						scale = 0.5F;
					}
					float projRelSurf = 1;
					float texTransY = baseTexTransY;
					switch (side) {
						case UP:
							projRelSurf = crosshairsY - y1;
							texTransY = (projRelSurf / (ri + projRelSurf)) + y1;
							break;
						case DOWN:
							projRelSurf = y1 - crosshairsY+1;
							texTransY = (projRelSurf / (ri + projRelSurf)) + y1;
							break;
					}
					glTranslatef(baseTexTransX, texTransY, baseTexTransZ);
					glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_OBJECT_LINEAR);
					glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_OBJECT_LINEAR);
					glTexGeni(GL_R, GL_TEXTURE_GEN_MODE, GL_OBJECT_LINEAR);
					glTexGeni(GL_Q, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
					glTexGenfv(GL_S, GL_OBJECT_PLANE, new float[] {1.0F, 0.0F, 0.0F, 0.0F});
					glTexGenfv(GL_T, GL_OBJECT_PLANE, new float[] {0.0F, 0.0F, 1.0F, 0.0F});
					glTexGenfv(GL_R, GL_OBJECT_PLANE, new float[] {0.0F, 0.0F, 0.0F, 1.0F});
					glTexGenfv(GL_Q, GL_EYE_PLANE, new float[] {0.0F, 1.0F, 0.0F, 0.0F});
					glEnable(GL_TEXTURE_GEN_S);
					glEnable(GL_TEXTURE_GEN_T);
					glEnable(GL_TEXTURE_GEN_R);
					glEnable(GL_TEXTURE_GEN_Q);
					glPopMatrix();
					glMatrixMode(GL_TEXTURE);
					glPushMatrix();
					glLoadIdentity();
					glTranslatef(0.0F, Util.getMeasuringTimeMs() % 700000L / 700000.0F, 0.0F);

					glScalef(scale, scale, scale);
					glTranslatef(0.5F, 0.5F, 0.0F);
					glRotatef((i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
					glTranslatef(-0.5F, -0.5F, 0.0F);
					glTranslatef(-baseTexTransX, -baseTexTransZ, -baseTexTransY);
					glTranslatef(crosshairsX * ri / projRelSurf, crosshairsZ * ri / projRelSurf, -baseTexTransY);

					float r = RANDOM.nextFloat() * 0.5F + 0.1F;
					float g = RANDOM.nextFloat() * 0.5F + 0.4F;
					float b = RANDOM.nextFloat() * 0.5F + 0.5F;
					if (i == 0) {
						b = 1.0F;
						g = 1.0F;
						r = 1.0F;
					}

					glColor4f(r * brightness, g * brightness, b * brightness, 1);
					glBegin(GL_QUADS);
					glVertex3d(x1, y1, z1);
					glVertex3d(x2, y1, z2);
					glVertex3d(x2, y2, z3);
					glVertex3d(x1, y2, z4);
					glEnd();
					glPopMatrix();
					glMatrixMode(GL_MODELVIEW);
				}

				glDisable(GL_BLEND);
				glDisable(GL_TEXTURE_GEN_S);
				glDisable(GL_TEXTURE_GEN_T);
				glDisable(GL_TEXTURE_GEN_R);
				glDisable(GL_TEXTURE_GEN_Q);
				glEnable(GL_LIGHTING);
				/*glPopMCMatrix*/ {
					glMatrixMode(GL_PROJECTION);
					glPopMatrix();
					glMatrixMode(GL_MODELVIEW);
					glPopMatrix();
				}
			}
		}
	}
}
