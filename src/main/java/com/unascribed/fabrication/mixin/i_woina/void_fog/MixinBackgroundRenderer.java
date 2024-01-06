package com.unascribed.fabrication.mixin.i_woina.void_fog;

import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
@EligibleIf(configAvailable="*.void_fog", envMatches=Env.CLIENT)
public abstract class MixinBackgroundRenderer {

	@Shadow
	private static float red;

	@Shadow
	private static float green;

	@Shadow
	private static float blue;

	private static float fabrication$voidFog = 1;

	@FabInject(method="render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V", at=@At("TAIL"))
	private static void fabrication$voidFogColor(Camera camera, float tickDelta, ClientWorld world, int i, float f, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.void_fog")) return;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null) return;
		Entity entity = client.cameraEntity;
		if (entity == null || world.isAir(entity.getBlockPos().withY(world.getBottomY()))) return;
		float fog = world.getLightLevel(LightType.SKY, entity.getBlockPos()) / 14f;
		float dist = Math.abs((float) entity.getY()-world.getBottomY());
		if (dist > 16f) fog += (dist - 16f)/10f;
		if (fog < 1f) {
			fabrication$voidFog = MathHelper.lerp(.05f, fabrication$voidFog, fog);
			red *= fog;
			green *= fog;
			blue *= fog;
			RenderSystem.clearColor(red, green, blue, 0);
		} else {
			fabrication$voidFog = MathHelper.lerp(.1f, fabrication$voidFog, 1);
		}
	}
	@FabInject(method="applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZF)V", at=@At("TAIL"))
	private static void fabrication$voidFogDistance(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.void_fog")) return;
		if (fogType != BackgroundRenderer.FogType.FOG_TERRAIN) return;;
		float fog = fabrication$voidFog;
		if (fog < 1.0) {
			if (fog < 0f) {
				fog = 0f;
			}
			fog *= fog * 100f;
			if (fog < 10f) {
				fog = 10f;
			}
			if (RenderSystem.getShaderFogEnd() > fog) {
				RenderSystem.setShaderFogEnd(fog);
			}
			if (RenderSystem.getShaderFogStart() > (fog *= .8f)) {
				RenderSystem.setShaderFogStart(fog);
			}
		}
	}
}
