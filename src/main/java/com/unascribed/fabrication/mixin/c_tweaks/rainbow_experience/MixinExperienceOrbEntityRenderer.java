package com.unascribed.fabrication.mixin.c_tweaks.rainbow_experience;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryRandom;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ExperienceOrbEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ExperienceOrbEntityRenderer.class)
@EligibleIf(configAvailable="*.rainbow_experience", envMatches=Env.CLIENT)
public class MixinExperienceOrbEntityRenderer {

	private final Random fabrication$colorDecider = ForgeryRandom.get();
	private int fabrication$orbColor6 = 0;
	private int fabrication$orbColor7 = 0;


	@FabInject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/ExperienceOrbEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void updateSeed(ExperienceOrbEntity experienceOrbEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.rainbow_experience")) return;
		fabrication$colorDecider.setSeed(experienceOrbEntity.getUuid().hashCode());
	}

	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/ExperienceOrbEntityRenderer;vertex(Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;Lorg/joml/Matrix3f;FFIIIFFI)V"),
			index=5, method="render(Lnet/minecraft/entity/ExperienceOrbEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public int addVertex5(int i) {
		if (!FabConf.isEnabled("*.rainbow_experience")) return i;
		float hue = fabrication$colorDecider.nextFloat();
		int color1 = MathHelper.hsvToRgb(hue, 0.8f, 1);
		int color2 = MathHelper.hsvToRgb(hue + (fabrication$colorDecider.nextBoolean() ? -0.08f : 0.08f), 0.8f, 1);
		float r1 = ((color1 >> 16) & 0xFF) / 255f;
		float g1 = ((color1 >> 8) & 0xFF) / 255f;
		float b1 = ((color1 >> 0) & 0xFF) / 255f;
		float r2 = ((color2 >> 16) & 0xFF) / 255f;
		float g2 = ((color2 >> 8) & 0xFF) / 255f;
		float b2 = ((color2 >> 0) & 0xFF) / 255f;
		float a = i / 255f;
		fabrication$orbColor6 = (int)((g1 + ((g2 - g1) * a)) * 255);
		fabrication$orbColor7 = (int)((b1 + ((b2 - b1) * a)) * 255);
		return (int)((r1 + ((r2 - r1) * a)) * 255);
	}
	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/ExperienceOrbEntityRenderer;vertex(Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;Lorg/joml/Matrix3f;FFIIIFFI)V"),
			index=6, method="render(Lnet/minecraft/entity/ExperienceOrbEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public int addVertex6(int i) {
		if (!FabConf.isEnabled("*.rainbow_experience")) return i;
		return fabrication$orbColor6;
	}
	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/ExperienceOrbEntityRenderer;vertex(Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;Lorg/joml/Matrix3f;FFIIIFFI)V"),
			index=7, method="render(Lnet/minecraft/entity/ExperienceOrbEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public int addVertex7(int i) {
		if (!FabConf.isEnabled("*.rainbow_experience")) return i;
		return fabrication$orbColor7;
	}

}
