package com.unascribed.fabrication.mixin.i_woina.blinking_drops;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.BlinkingDropsOverlay;
import com.unascribed.fabrication.logic.WoinaDrops;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.FailOn;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
@EligibleIf(configAvailable="*.blinking_drops", envMatches=Env.CLIENT)
@FailOn(modLoaded="forge:obfuscate")
public class MixinItemEntityRendererVanilla {

	@Unique
	private static final ThreadLocal<Integer> captureItemHash = new ThreadLocal<>();

	@FabInject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void captureHash(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
		if (!FabConf.isEnabled("*.blinking_drops")) return;
		captureItemHash.set(itemEntity.hashCode());
	}

	@FabInject(at=@At(value="INVOKE", shift=At.Shift.BEFORE, target="Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"),
			method="renderStack(Lnet/minecraft/client/render/item/ItemRenderer;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/BakedModel;ZLnet/minecraft/util/math/random/Random;)V")
	private static void markAsDrop(ItemRenderer itemRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, BakedModel model, boolean depth, Random random, CallbackInfo ci){
		if (!FabConf.isEnabled("*.blinking_drops")) return;
		BlinkingDropsOverlay.isDropped = true;
	}

	@FabInject(at=@At(value="INVOKE", shift=At.Shift.AFTER, target="Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"),
			method="renderStack(Lnet/minecraft/client/render/item/ItemRenderer;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/BakedModel;ZLnet/minecraft/util/math/random/Random;)V")
	private static void removeDropMark(ItemRenderer itemRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, BakedModel model, boolean depth, Random random, CallbackInfo ci){
		if (!FabConf.isEnabled("*.blinking_drops")) return;
		BlinkingDropsOverlay.isDropped = false;
	}

	@ModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"),
			method="renderStack(Lnet/minecraft/client/render/item/ItemRenderer;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/BakedModel;ZLnet/minecraft/util/math/random/Random;)V", index=6)
	private static int blink(int old){
		if (!FabConf.isEnabled("*.blinking_drops")) return old;
		Integer i = captureItemHash.get();
		return i == null ? old : WoinaDrops.modifyOverlay(captureItemHash.get(), old);
	}

}
