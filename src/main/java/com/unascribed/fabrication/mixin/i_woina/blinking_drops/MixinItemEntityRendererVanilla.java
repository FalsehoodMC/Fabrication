package com.unascribed.fabrication.mixin.i_woina.blinking_drops;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.BlinkingDropsOverlay;
import com.unascribed.fabrication.logic.WoinaDrops;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
@EligibleIf(configAvailable="*.blinking_drops", envMatches=Env.CLIENT, modNotLoaded="forge:obfuscate")
public class MixinItemEntityRendererVanilla {

	private int fabrication$captureItemHash;

	@FabInject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void captureHash(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
		if (!FabConf.isEnabled("*.blinking_drops")) return;
		fabrication$captureItemHash = itemEntity.hashCode();
	}

	@FabInject(at=@At(value="INVOKE", shift=At.Shift.BEFORE, target="Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"),
			method="render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void markAsDrop(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
		if (!FabConf.isEnabled("*.blinking_drops")) return;
		BlinkingDropsOverlay.isDropped = true;
	}

	@FabInject(at=@At(value="INVOKE", shift=At.Shift.AFTER, target="Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"),
			method="render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void removeDropMark(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
		if (!FabConf.isEnabled("*.blinking_drops")) return;
		BlinkingDropsOverlay.isDropped = false;
	}

	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"),
			method="render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", index=6)
	public int blink(int old){
		if (!FabConf.isEnabled("*.blinking_drops")) return old;
		return WoinaDrops.modifyOverlay(fabrication$captureItemHash, old);
	}

}
