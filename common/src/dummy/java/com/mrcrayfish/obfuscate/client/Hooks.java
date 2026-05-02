package com.mrcrayfish.obfuscate.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

public class Hooks {

	// Dummy.

	public static void fireRenderEntityItem(ItemRenderer renderer, ItemStack stack, ModelTransformation.Mode transformType, boolean leftHanded, MatrixStack matrixStack, VertexConsumerProvider renderTypeBuffer, int light, int overlay, BakedModel model, ItemEntity entity, float partialTicks) {

	}

}
