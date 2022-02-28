package com.unascribed.fabrication.mixin.a_fixes.fix_end_portal_render;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EndPortalBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.fix_end_portal_render", envMatches=Env.CLIENT)
public abstract class MixinPortalBlockEntityRenderer {

	@Shadow
	protected abstract float getTopYOffset();

	@Shadow
	protected abstract float getBottomYOffset();

	@ModifyVariable(ordinal=2, at=@At("HEAD"), method="renderSide(Lnet/minecraft/block/entity/EndPortalBlockEntity;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFLnet/minecraft/util/math/Direction;)V")
	public float fixSideRender1(float y) {
		if (FabConf.isEnabled("*.fix_end_portal_render")) {
			if (y == 1) return this.getTopYOffset();
			else if (y == 0) return this.getBottomYOffset();
		}
		return y;
	}
	@ModifyVariable(ordinal=3, at=@At("HEAD"), method="renderSide(Lnet/minecraft/block/entity/EndPortalBlockEntity;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFLnet/minecraft/util/math/Direction;)V")
	public float fixSideRender2(float y) {
		if (FabConf.isEnabled("*.fix_end_portal_render")) {
			if (y == 1) return this.getTopYOffset();
			else if (y == 0) return this.getBottomYOffset();
		}
		return y;
	}
}
