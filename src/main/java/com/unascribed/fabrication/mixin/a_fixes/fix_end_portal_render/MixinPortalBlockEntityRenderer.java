package com.unascribed.fabrication.mixin.a_fixes.fix_end_portal_render;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(EndPortalBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.fix_end_portal_render", envMatches=Env.CLIENT)
public abstract class MixinPortalBlockEntityRenderer {

	@Shadow
	protected abstract float getTopYOffset();

	@Shadow
	protected abstract float getBottomYOffset();

	@ModifyVariable(ordinal=2, at=@At("HEAD"), method="renderSide(Lnet/minecraft/block/entity/EndPortalBlockEntity;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFLnet/minecraft/util/math/Direction;)V")
	public float fixSideRender1(float y) {
		if (MixinConfigPlugin.isEnabled("*.fix_end_portal_render")) {
			if (y == 1) return this.getTopYOffset();
			else if (y == 0) return this.getBottomYOffset();
		}
		return y;
	}
	@ModifyVariable(ordinal=3, at=@At("HEAD"), method="renderSide(Lnet/minecraft/block/entity/EndPortalBlockEntity;Lnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;FFFFFFFFLnet/minecraft/util/math/Direction;)V")
	public float fixSideRender2(float y) {
		if (MixinConfigPlugin.isEnabled("*.fix_end_portal_render")) {
			if (y == 1) return this.getTopYOffset();
			else if (y == 0) return this.getBottomYOffset();
		}
		return y;
	}
}
