package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;

@Mixin(LivingEntityRenderer.class)
@EligibleIf(configAvailable="*.foliage_creepers")
public abstract class MixinLivingEntityRenderer {

	LivingEntity fabrication$capturedRenderEntity;

	@Inject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void captureEntity(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
		fabrication$capturedRenderEntity = livingEntity;
	}
	@ModifyArgs(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"),
			method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void render(Args args) {
		if (!(MixinConfigPlugin.isEnabled("*.foliage_creepers") && fabrication$capturedRenderEntity instanceof CreeperEntity)) return;
		int i = fabrication$capturedRenderEntity.world.getBiome(fabrication$capturedRenderEntity.getBlockPos()).getFoliageColor();
		args.set(4, (i >> 16 & 255) / 255f);
		args.set(5, (i >> 8 & 255) / 255.0F);
		args.set(6, (i & 255) / 255.0F);
	}

}
