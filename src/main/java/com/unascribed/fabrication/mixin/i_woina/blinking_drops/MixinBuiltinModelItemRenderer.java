package com.unascribed.fabrication.mixin.i_woina.blinking_drops;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.BlinkingDropsOverlay;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinModelItemRenderer.class)
@EligibleIf(configAvailable="*.blinking_drops", envMatches=Env.CLIENT, modNotLoaded="forge:obfuscate")
public class MixinBuiltinModelItemRenderer {

	@FabInject(at=@At(value="INVOKE", shift=At.Shift.BEFORE, target="Lnet/minecraft/client/render/block/entity/SkullBlockEntityRenderer;render(Lnet/minecraft/util/math/Direction;FLnet/minecraft/block/SkullBlock$SkullType;Lcom/mojang/authlib/GameProfile;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"),
			method="render(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V")
	public void blinkSkull(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci){
		if (!FabConf.isEnabled("*.blinking_drops")) return;
		BlinkingDropsOverlay.newOverlay = stack == null ? 0 : stack.hashCode();
	}

}
