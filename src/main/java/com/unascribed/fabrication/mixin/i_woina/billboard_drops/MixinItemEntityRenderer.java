package com.unascribed.fabrication.mixin.i_woina.billboard_drops;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.client.FlatItems;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

@Mixin(ItemEntityRenderer.class)
@EligibleIf(configAvailable="*.billboard_drops", envMatches=Env.CLIENT)
public class MixinItemEntityRenderer {

	@FabInject(at=@At(value="INVOKE", target="net/minecraft/client/util/math/MatrixStack.push()V", shift=Shift.AFTER),
			method="renderStack(Lnet/minecraft/client/render/item/ItemRenderer;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/BakedModel;ZLnet/minecraft/util/math/random/Random;)V")
	private static void render(ItemRenderer itemRenderer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStack stack, BakedModel model, boolean depth, Random random, CallbackInfo ci) {
		if (FabConf.isEnabled("*.billboard_drops") && FlatItems.hasGeneratedModel(stack)) {
			matrices.scale(1, 1, 0.01f);
		}
	}

}
