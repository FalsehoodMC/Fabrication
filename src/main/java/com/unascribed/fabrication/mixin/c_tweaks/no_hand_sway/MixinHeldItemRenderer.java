package com.unascribed.fabrication.mixin.c_tweaks.no_hand_sway;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.item.HeldItemRenderer;

@Mixin(HeldItemRenderer.class)
@EligibleIf(configAvailable="*.no_hand_sway", envMatches=Env.CLIENT)
public abstract class MixinHeldItemRenderer {

	@FabModifyArg(method= "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
			at=@At(value = "INVOKE", target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;", ordinal = 0), index = 0)
	private float setH(float h) {
		return FabConf.isEnabled("*.no_hand_sway") ? 1 : h;
	}
	@FabModifyArg(method= "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
			at=@At(value = "INVOKE", target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;", ordinal = 1), index = 0)
	private float setI(float i) {
		return FabConf.isEnabled("*.no_hand_sway") ? 1 : i;
	}

}
