package com.unascribed.fabrication.mixin.a_fixes.inanimates_can_be_invisible;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Mixin({BoatEntityRenderer.class, MinecartEntityRenderer.class})
@EligibleIf(configEnabled="*.inanimates_can_be_invisible", envMatches=Env.CLIENT)
public class MixinBoatAndMinecartEntityRenderer {

	// for rendering a player's own mounted vehicle
	
	@Inject(at=@At("HEAD"), method="render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", cancellable=true, expect=1)
	public void render(Entity e, float f, float f2, MatrixStack matricies, VertexConsumerProvider vcp, int i, CallbackInfo ci) {
		if (e.isInvisible()) {
			ci.cancel();
		}
	}
	
}
