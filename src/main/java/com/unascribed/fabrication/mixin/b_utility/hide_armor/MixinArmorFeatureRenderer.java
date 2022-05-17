package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.GetSuppressedSlots;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

@Mixin(ArmorFeatureRenderer.class)
@EligibleIf(configAvailable="*.hide_armor", envMatches=Env.CLIENT)
public class MixinArmorFeatureRenderer {

	@Inject(at=@At("HEAD"), method="renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V",
			cancellable=true)
	private void renderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, LivingEntity entity, EquipmentSlot slot, int i, BipedEntityModel<? extends LivingEntity> model, CallbackInfo ci) {
		if (FabConf.isEnabled("*.hide_armor") && entity instanceof GetSuppressedSlots
				&& ((GetSuppressedSlots)entity).fabrication$getSuppressedSlots().contains(slot)) {
			ci.cancel();
		}
	}

}
