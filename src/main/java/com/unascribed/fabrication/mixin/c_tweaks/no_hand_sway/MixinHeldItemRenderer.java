package com.unascribed.fabrication.mixin.c_tweaks.no_hand_sway;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.render.item.HeldItemRenderer;

@Mixin(HeldItemRenderer.class)
@EligibleIf(configAvailable="*.no_hand_sway", envMatches=Env.CLIENT)
public abstract class MixinHeldItemRenderer {

	@ModifyArg(method= "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
			at=@At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3f;getDegreesQuaternion(F)Lnet/minecraft/util/math/Quaternion;", ordinal = 0), index = 0)
	private float setH(float h) {
		return MixinConfigPlugin.isEnabled("*.no_hand_sway") ? 1 : h;
	}
	@ModifyArg(method= "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
			at=@At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3f;getDegreesQuaternion(F)Lnet/minecraft/util/math/Quaternion;", ordinal = 1), index = 0)
	private float setI(float i) {
		return MixinConfigPlugin.isEnabled("*.no_hand_sway") ? 1 : i;
	}

}
