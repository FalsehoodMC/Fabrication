package com.unascribed.fabrication.mixin.c_tweaks.rainbow_experience;

import java.util.Random;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.ExperienceOrbEntityRenderer;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

@Mixin(ExperienceOrbEntityRenderer.class)
@EligibleIf(configAvailable="*.rainbow_experience", envMatches=Env.CLIENT)
public class MixinExperienceOrbEntityRenderer {

	private final Random fabrication$colorDecider = new Random();

	@Shadow
	private static void method_23171(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, float f, float g, int i, int j, int k, float h, float l, int m) {}

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/client/render/entity/ExperienceOrbEntityRenderer.method_23171(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/util/math/Matrix3f;FFIIIFFI)V"),
			method="render(Lnet/minecraft/entity/ExperienceOrbEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void addVertex(VertexConsumer arg1, Matrix4f arg2, Matrix3f arg3, float arg4, float arg5, int r, int g, int b, float arg9, float arg10, int arg11, ExperienceOrbEntity entity) {
		if (FabConf.isEnabled("*.rainbow_experience")) {
			fabrication$colorDecider.setSeed(entity.getUuid().hashCode());
			float hue = fabrication$colorDecider.nextFloat();
			int color1 = MathHelper.hsvToRgb(hue, 0.8f, 1);
			int color2 = MathHelper.hsvToRgb(hue+(fabrication$colorDecider.nextBoolean() ? -0.08f : 0.08f), 0.8f, 1);
			float r1 = ((color1>>16)&0xFF)/255f;
			float g1 = ((color1>>8)&0xFF)/255f;
			float b1 = ((color1>>0)&0xFF)/255f;
			float r2 = ((color2>>16)&0xFF)/255f;
			float g2 = ((color2>>8)&0xFF)/255f;
			float b2 = ((color2>>0)&0xFF)/255f;
			float a = r/255f;
			r = (int)((r1+((r2-r1)*a))*255);
			g = (int)((g1+((g2-g1)*a))*255);
			b = (int)((b1+((b2-b1)*a))*255);
		}
		method_23171(arg1, arg2, arg3, arg4, arg5, r, g, b, arg9, arg10, arg11);
	}

}
