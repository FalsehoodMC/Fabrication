package com.unascribed.fabrication.mixin.b_utility.mob_ids;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

@Mixin(EntityRenderer.class)
@EligibleIf(configAvailable="*.mob_ids", envMatches=Env.CLIENT)
public abstract class MixinEntityRenderer {

	@Unique
	private boolean fabrication$mobIdsShiftTextDown;

	@Shadow
	protected abstract void renderLabelIfPresent(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vcp, int light);

	@FabInject(at=@At("TAIL"), method="render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void render(Entity e, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.mob_ids")) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player != null && mc.player.isCreative() && mc.options.debugEnabled) {
			matrices.push();
			matrices.translate(0, ((e.getHeight()+0.5f)/2), 0);
			matrices.scale(0.5f, 0.5f, 0.5f);
			try {
				fabrication$mobIdsShiftTextDown = true;
				renderLabelIfPresent(e, Text.literal(e.getUuidAsString()), matrices, vcp, light);
			} finally {
				fabrication$mobIdsShiftTextDown = false;
			}
			matrices.pop();
		}
	}

	@FabInject(at=@At(value="INVOKE", target="net/minecraft/client/util/math/MatrixStack.scale(FFF)V", shift=At.Shift.AFTER),
			method="renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void renderLabelIfPresentAdjustPosition(Entity e, Text text, MatrixStack matrices, VertexConsumerProvider vcp, int light, CallbackInfo ci) {
		if (fabrication$mobIdsShiftTextDown) {
			matrices.translate(0, 19, 0);
		}
	}

}
