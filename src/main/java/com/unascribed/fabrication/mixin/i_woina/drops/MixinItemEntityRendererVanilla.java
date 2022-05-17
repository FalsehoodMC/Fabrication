package com.unascribed.fabrication.mixin.i_woina.drops;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.Hijack;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.logic.WoinaDrops;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

@Mixin(ItemEntityRenderer.class)
@EligibleIf(configAvailable="*.classic_block_drops", envMatches=Env.CLIENT, modNotLoaded="forge:obfuscate")
public class MixinItemEntityRendererVanilla {

	@Hijack(target="Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
			method="render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	private static boolean fabrication$renderClassicBlockDrops(ItemRenderer subject, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
		if (FabConf.isEnabled("*.classic_block_drops")) {
			WoinaDrops.interceptRender(subject, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
			return true;
		}
		return false;
	}

}
