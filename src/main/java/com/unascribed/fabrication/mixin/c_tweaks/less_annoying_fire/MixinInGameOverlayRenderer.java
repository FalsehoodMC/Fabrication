package com.unascribed.fabrication.mixin.c_tweaks.less_annoying_fire;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;

@Mixin(InGameOverlayRenderer.class)
@EligibleIf(configEnabled="*.less_annoying_fire", envMatches=Env.CLIENT)
public class MixinInGameOverlayRenderer {

	@Inject(at=@At("HEAD"), method="renderFireOverlay(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/util/math/MatrixStack;)V", cancellable=true)
	private static void renderFireOverlayHead(MinecraftClient client, MatrixStack stack, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.less_annoying_fire")) return;
		if (client.player.isInvulnerableTo(DamageSource.ON_FIRE) || client.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
			ci.cancel();
		} else {
			stack.push();
			stack.translate(0, -0.2, 0);
		}
	}

	@Inject(at=@At("TAIL"), method="renderFireOverlay(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/util/math/MatrixStack;)V", cancellable=true)
	private static void renderFireOverlayTail(MinecraftClient client, MatrixStack stack, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.less_annoying_fire")) return;
		stack.pop();
	}

}
