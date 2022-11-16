package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
@EligibleIf(configAvailable="*.foliage_creepers", envMatches=Env.CLIENT)
public abstract class MixinLivingEntityRenderer extends EntityRenderer<LivingEntity> {

	private static final Identifier fabrication$creeperTexture = new Identifier("textures/entity/creeper/creeper.png");
	int fabrication$colorFoliageCreeper = -1;

	protected MixinLivingEntityRenderer(EntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}


	@FabInject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void captureEntity(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
		if (FabConf.isEnabled("*.foliage_creepers") && fabrication$creeperTexture.equals(this.getTexture(livingEntity))) {
			fabrication$colorFoliageCreeper = livingEntity.world.getColor(livingEntity.getBlockPos(), BiomeColors.FOLIAGE_COLOR);
		} else if (fabrication$colorFoliageCreeper != -1) {
			fabrication$colorFoliageCreeper = -1;
		}
	}

	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index=4,
			method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public float changeColor4(float f) {
		if (fabrication$colorFoliageCreeper == -1) return f;
		return (fabrication$colorFoliageCreeper >> 16 & 255) / 255f;
	}
	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index=5,
			method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public float changeColor5(float f) {
		if (fabrication$colorFoliageCreeper == -1) return f;
		return (fabrication$colorFoliageCreeper >> 8 & 255) / 255f;
	}
	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), index=6,
			method="render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public float changeColor6(float f) {
		if (fabrication$colorFoliageCreeper == -1) return f;
		return (fabrication$colorFoliageCreeper & 255) / 255f;
	}

	@FabModifyVariable(at=@At("STORE"), method="getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;")
	public Identifier transformCreeperIdentifier(Identifier id){
		if (FabConf.isEnabled("*.foliage_creepers") && fabrication$creeperTexture.equals(id)) {
			return new Identifier("fabrication_grayscale", id.getPath());
		}
		return id;
	}

}
