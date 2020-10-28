package com.unascribed.fabrication.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.RenderingAgeAccess;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntityRenderer.class)
@EligibleIf(configEnabled="*.despawning_items_blink", envMatches=Env.CLIENT)
public class MixinDespawningItemsBlinkRenderer {

	@Inject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
			cancellable=true)
	public void render(ItemEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (RuntimeChecks.check("*.despawning_items_blink") && entity instanceof RenderingAgeAccess) {
			RenderingAgeAccess aa = (RenderingAgeAccess)entity;
			int age = aa.fabrication$getRenderingAge();
			int timeUntilDespawn = 6000-age;
			if (timeUntilDespawn < 40) {
				// 1 second; blink every tick
				if (age%2 == 0) {
					ci.cancel();
				}
			} else if (timeUntilDespawn < 40) {
				// 2 seconds; blink every quarter second
				if ((age/5)%2 == 0) {
					ci.cancel();
				}
			} else if (timeUntilDespawn < 100) {
				// 5 seconds; blink every half second for a quarter second
				if (age%10 < 5) {
					ci.cancel();
				}
			} else if (timeUntilDespawn < 200) {
				// 10 seconds; blink every second for a quarter second
				if (age%20 < 5) {
					ci.cancel();
				}
			}
		}
	}
	
}
