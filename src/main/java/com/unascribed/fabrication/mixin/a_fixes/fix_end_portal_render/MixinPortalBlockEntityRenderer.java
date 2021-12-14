package com.unascribed.fabrication.mixin.a_fixes.fix_end_portal_render;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EndPortalBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.fix_end_portal_render", envMatches=Env.CLIENT)
public abstract class MixinPortalBlockEntityRenderer {

	@Shadow
	protected abstract float method_3594();

	@ModifyVariable(ordinal=2, at=@At("HEAD"), method="method_23085(Lnet/minecraft/block/entity/EndPortalBlockEntity;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFFFFLnet/minecraft/util/math/Direction;)V")
	public float fixSideRender1(float y) {
		if (MixinConfigPlugin.isEnabled("*.fix_end_portal_render")) {
			if (y == 1) return this.method_3594();
			else if (y == 0) return 0.375f;
		}
		return y;
	}
	@ModifyVariable(ordinal=3, at=@At("HEAD"), method="method_23085(Lnet/minecraft/block/entity/EndPortalBlockEntity;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFFFFLnet/minecraft/util/math/Direction;)V")
	public float fixSideRender2(float y) {
		if (MixinConfigPlugin.isEnabled("*.fix_end_portal_render")) {
			if (y == 1) return this.method_3594();
			else if (y == 0) return 0.375f;
		}
		return y;
	}
}
