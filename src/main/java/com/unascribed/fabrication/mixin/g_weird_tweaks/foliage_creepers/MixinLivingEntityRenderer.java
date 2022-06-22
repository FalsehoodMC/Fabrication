package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
@EligibleIf(configAvailable="*.foliage_creepers", envMatches=Env.CLIENT)
public abstract class MixinLivingEntityRenderer {

	LivingEntity fabrication$capturedRenderEntity;

	@FabInject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void captureEntity(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
		fabrication$capturedRenderEntity = livingEntity;
	}

	@FabModifyArgs(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"),
			method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void render(Args args) {
		if (!(FabConf.isEnabled("*.foliage_creepers") && fabrication$capturedRenderEntity instanceof CreeperEntity)) return;
		int i = fabrication$capturedRenderEntity.world.getColor(fabrication$capturedRenderEntity.getBlockPos(), BiomeColors.FOLIAGE_COLOR);
		args.set(4, (i >> 16 & 255) / 255f);
		args.set(5, (i >> 8 & 255) / 255f);
		args.set(6, (i & 255) / 255f);
	}

	@FabModifyVariable(at=@At("STORE"), method="getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;")
	public Identifier transformCreeperIdentifier(Identifier id){
		if (!(FabConf.isEnabled("*.foliage_creepers") && ((Object)this) instanceof CreeperEntityRenderer && Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace()))) return id;
		return new Identifier("fabrication_grayscale", id.getPath());
	}

}
