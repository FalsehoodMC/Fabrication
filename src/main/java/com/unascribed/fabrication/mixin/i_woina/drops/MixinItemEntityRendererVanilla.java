package com.unascribed.fabrication.mixin.i_woina.drops;

import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.injection.UnnamedMagic;
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
@EligibleIf(anyConfigAvailable= {"*.blinking_drops", "*.classic_block_drops"}, envMatches=Env.CLIENT, modNotLoaded="forge:obfuscate")
public class MixinItemEntityRendererVanilla {

	@UnnamedMagic(target={"Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", "Lnet/minecraft/class_918;method_23179(Lnet/minecraft/class_1799;Lnet/minecraft/class_809$class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;IILnet/minecraft/class_1087;)V"},
			method={"render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", "Lnet/minecraft/class_916;method_3996(Lnet/minecraft/class_1542;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"})
	private static boolean fabrication$renderClassicBlockDrops(ItemRenderer subject, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
		if (MixinConfigPlugin.isEnabled("*.classic_block_drops")) {
			WoinaDrops.interceptRender(subject, stack, renderMode, leftHanded, matrices, vertexConsumers, light, WoinaDrops.modifyOverlay(stack, overlay), model);
			return true;
		}
		return false;
	}

}
