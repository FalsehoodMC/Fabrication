package com.unascribed.fabrication.mixin.i_woina.janky_arm;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3f;

@Mixin(HeldItemRenderer.class)
@EligibleIf(configAvailable="*.janky_arm", envMatches=Env.CLIENT)
public class MixinHeldItemRenderer {

	@Inject(at=@At(value="CONSTANT", args="floatValue=-20"),
			method="renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V")
	private void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
		float f = arm == Arm.LEFT ? -1 : 1;
		if (FabConf.isEnabled("*.janky_arm")) {
			matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-33.0F));
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(f*-35.0F));
			matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(f*-8f));
			matrices.translate(0.07*f, 0.13, -0.04);
		}
	}

}
