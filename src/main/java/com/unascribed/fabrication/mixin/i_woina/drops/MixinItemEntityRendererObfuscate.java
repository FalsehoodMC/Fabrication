package com.unascribed.fabrication.mixin.i_woina.drops;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mrcrayfish.obfuscate.client.Hooks;
import com.unascribed.fabrication.logic.WoinaDrops;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

@Mixin(value=Hooks.class)
@EligibleIf(anyConfigAvailable= {"*.blinking_drops", "*.classic_block_drops"}, envMatches=Env.CLIENT, modLoaded="forge:obfuscate")
public class MixinItemEntityRendererObfuscate {

	@FabModifyVariable(at=@At("HEAD"), method="fireRenderEntityItem",
			index=7, argsOnly=true, remap=false)
	private static int modifyOverlay(int orig, ItemRenderer subject, ItemStack stack) {
		return WoinaDrops.modifyOverlay(stack.hashCode(), orig);
	}

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/client/render/item/ItemRenderer.renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", remap=true),
			method="fireRenderEntityItem", remap=false)
	private static void interceptRender(ItemRenderer subject, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
		WoinaDrops.interceptRender(subject, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
	}

}
