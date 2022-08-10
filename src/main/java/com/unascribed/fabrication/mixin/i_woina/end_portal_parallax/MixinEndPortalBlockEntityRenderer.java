package com.unascribed.fabrication.mixin.i_woina.end_portal_parallax;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.client.GLUPort;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;

import java.util.Random;
import static com.unascribed.lib39.deferral.api.RenderBridge.*;

@Environment(EnvType.CLIENT)
@Mixin(EndPortalBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.end_portal_parallax", specialConditions=SpecialEligibility.NOT_MACOS, modNotLoaded="endlessencore")
public abstract class MixinEndPortalBlockEntityRenderer {

	@Shadow
	@Final
	public static Identifier PORTAL_TEXTURE;
	@Shadow
	@Final
	public static Identifier SKY_TEXTURE;


	@Shadow
	protected abstract float getTopYOffset();
	@Shadow
	protected abstract float getBottomYOffset();
	
	@Unique
	private static final Random RANDOM = new Random(31100L);

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	public void fabrication$render(EndPortalBlockEntity be, float tickDelta, MatrixStack poseStack, VertexConsumerProvider multiBufferSource, int light, int overlay, CallbackInfo ci) {
		if (canUseCompatFunctions() && FabConf.isEnabled("*.end_portal_parallax")) {
			// YOU try to make it work with the gateway, I DARE YOU
			if (be instanceof EndGatewayBlockEntity) return;
			
			ci.cancel();
			// ABANDON  ALL HOPE
			// YE WHO ENTER HERE
			MinecraftClient mc = MinecraftClient.getInstance();
			try (var stack = MemoryStack.stackPush()) {
				var pos = be.getPos();
				float blockX = 0;
				float blockY = 0;
				float blockZ = 0;
				
				var viewportBuffer = stack.mallocInt(4);
				var scratchBuffer = stack.mallocFloat(16);
				var crosshairsPos = stack.mallocFloat(3);
				var projBuffer = stack.mallocFloat(16);

				glGetIntegerv(GL_VIEWPORT, viewportBuffer);
				float viewportCX = (viewportBuffer.get(0) + viewportBuffer.get(2)) / 2;
				float viewportCY = (viewportBuffer.get(1) + viewportBuffer.get(3)) / 2;
				var scratchMat = RenderSystem.getModelViewMatrix().copy();
				var projMatrix = RenderSystem.getProjectionMatrix().copy();
				scratchMat.multiply(poseStack.peek().getPositionMatrix());
				projMatrix.writeColumnMajor(projBuffer);
				scratchMat.writeColumnMajor(scratchBuffer);

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
				glPushMCMatrix(poseStack);
				glEnable(GL_TEXTURE_2D);
				glEnable(GL_DEPTH_TEST);
				glDepthFunc(GL_LEQUAL);
				glDisable(GL_LIGHTING);
				
				float topY = getTopYOffset();
				float bottomY = getBottomYOffset();
				
				RANDOM.setSeed(31100L);
				for (int i = 0; i < 16; ++i) {
					glPushMatrix();
					float ri = 16 - i;
					float scale = 0.0625f;
					float brightness = 1 / (ri + 1f);
					if (i == 0) {
						mc.getTextureManager().bindTexture(SKY_TEXTURE);
						brightness = 0.1f;
						ri = 65;
						scale = 0.125f;
						glEnable(GL_BLEND);
						glDefaultBlendFunc();
					}

					if (i == 1) {
						mc.getTextureManager().bindTexture(PORTAL_TEXTURE);
						glEnable(GL_BLEND);
						glBlendFunc(GL_ONE, GL_ONE);
						scale = 0.5f;
					}

					float crosshairs = crosshairsY;
					float surface = topY;
					float projRelSurf = surface + crosshairs; // projection relative surface
					float layerY = surface + ri + crosshairs;
					float texTransY = projRelSurf / layerY;
					texTransY += blockY + topY;
					glTranslatef(baseTexTransX, texTransY, baseTexTransZ);
					glPushMatrix();
						glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_OBJECT_LINEAR);
						glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_OBJECT_LINEAR);
						glTexGeni(GL_Q, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
						glTexGenfv(GL_S, GL_OBJECT_PLANE, new float[] {1, 0, 0, 1});
						glTexGenfv(GL_T, GL_OBJECT_PLANE, new float[] {0, 0, 1, 1});
						glTexGenfv(GL_Q, GL_EYE_PLANE,    new float[] {0, 1, 0, 0});
					glPopMatrix();
					glEnable(GL_TEXTURE_GEN_S);
					glEnable(GL_TEXTURE_GEN_T);
					glEnable(GL_TEXTURE_GEN_Q);
					glPopMatrix();
					glMatrixMode(GL_TEXTURE);
					glPushMatrix();
					glLoadIdentity();
					glTranslatef(0F, Util.getMeasuringTimeMs() % 700000 / 700000f, 0);
					glScalef(scale, scale, scale);
					glTranslatef(0.5f, 0.5f, 0);
					glRotatef((i * i * 4321 + i * 9) * 2, 0, 0, 1);
					glTranslatef(-0.5f, -0.5f, 0);
					glTranslatef(-baseTexTransX, -baseTexTransZ, -baseTexTransY);
					projRelSurf = surface + crosshairsY;
					glTranslatef(crosshairsX * ri / projRelSurf, crosshairsZ * ri / projRelSurf, -baseTexTransY);
					float r = RANDOM.nextFloat() * 0.5f + 0.1f;
					float g = RANDOM.nextFloat() * 0.5f + 0.4f;
					float b = RANDOM.nextFloat() * 0.5f + 0.5f;
					if (i == 0) {
						r = g = b = 1;
					}

					glColor4f(r * brightness, g * brightness, b * brightness, 1);
					glBegin(GL_QUADS);
						glVertex3d(blockX  , blockY + topY, blockZ  );
						glVertex3d(blockX  , blockY + topY, blockZ+1);
						glVertex3d(blockX+1, blockY + topY, blockZ+1);
						glVertex3d(blockX+1, blockY + topY, blockZ  );
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
				glPopMCMatrix();
			}
		}
	}
}
