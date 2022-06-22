package com.unascribed.fabrication.mixin.i_woina.billboard_drops;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntityRenderer.class)
@EligibleIf(configAvailable="*.billboard_drops", envMatches=Env.CLIENT)
public class MixinItemEntityRenderer {

	@Shadow @Final
	private ItemRenderer itemRenderer;

	@FabInject(at=@At(value="INVOKE", target="net/minecraft/client/util/math/MatrixStack.push()V", shift=Shift.AFTER, ordinal=1),
			method="render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void render(ItemEntity entity, float f, float g, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		if (FabConf.isEnabled("*.billboard_drops") && FlatItems.hasGeneratedModel(entity.getStack())) {
			matrices.scale(1, 1, 0.01f);
		}
	}

}
