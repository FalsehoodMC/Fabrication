package com.unascribed.fabrication.mixin.i_woina.drops;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.WoinaDrops;
import com.unascribed.fabrication.interfaces.RenderingAgeAccess;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntityRenderer.class)
@EligibleIf(anyConfigEnabled= {"*.blinking_drops", "*.classic_block_drops"}, envMatches=Env.CLIENT)
public class MixinItemEntityRenderer {

	@Inject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
			cancellable=true)
	public void render(ItemEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.blinking_drops")) {
			if (MixinConfigPlugin.isEnabled("*.despawning_items_blink") && entity instanceof RenderingAgeAccess) {
				RenderingAgeAccess aa = (RenderingAgeAccess)entity;
				float m = 1;
				int age = aa.fabrication$getRenderingAge();
				int timeUntilDespawn = 6000-age;
				if (timeUntilDespawn < 100) {
					m += 0.5f+(1-(timeUntilDespawn/100f))*4;
				} else if (timeUntilDespawn < 200) {
					m += (1-(timeUntilDespawn/200f));
				}
				WoinaDrops.curTimer = (entity.age+tickDelta)*m;
			}
		}
	}
	
}
