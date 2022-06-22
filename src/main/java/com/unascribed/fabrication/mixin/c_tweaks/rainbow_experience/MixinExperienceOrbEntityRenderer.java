package com.unascribed.fabrication.mixin.c_tweaks.rainbow_experience;

import java.util.Random;

import com.unascribed.fabrication.FabConf;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.FabModifyArgs;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.entity.ExperienceOrbEntityRenderer;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ExperienceOrbEntityRenderer.class)
@EligibleIf(configAvailable="*.rainbow_experience", envMatches=Env.CLIENT)
public class MixinExperienceOrbEntityRenderer {

	private final Random fabrication$colorDecider = new Random();

	@FabInject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/ExperienceOrbEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void updateSeed(ExperienceOrbEntity experienceOrbEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.rainbow_experience")) return;
		fabrication$colorDecider.setSeed(experienceOrbEntity.getUuid().hashCode());
	}

	@FabModifyArgs(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/ExperienceOrbEntityRenderer;vertex(Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/util/math/Matrix3f;FFIIIFFI)V"),
			method="render(Lnet/minecraft/entity/ExperienceOrbEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void addVertex(Args args) {
		if (FabConf.isEnabled("*.rainbow_experience")) {
			float hue = fabrication$colorDecider.nextFloat();
			int color1 = MathHelper.hsvToRgb(hue, 0.8f, 1);
			int color2 = MathHelper.hsvToRgb(hue+(fabrication$colorDecider.nextBoolean() ? -0.08f : 0.08f), 0.8f, 1);
			float r1 = ((color1>>16)&0xFF)/255f;
			float g1 = ((color1>>8)&0xFF)/255f;
			float b1 = ((color1>>0)&0xFF)/255f;
			float r2 = ((color2>>16)&0xFF)/255f;
			float g2 = ((color2>>8)&0xFF)/255f;
			float b2 = ((color2>>0)&0xFF)/255f;
			float a = ((int)args.get(5))/255f;
			args.set(5, (int)((r1+((r2-r1)*a))*255));
			args.set(6, (int)((g1+((g2-g1)*a))*255));
			args.set(7, (int)((b1+((b2-b1)*a))*255));
		}
	}

}
