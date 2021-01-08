package com.unascribed.fabrication.mixin.c_tweaks.legible_signs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;

@Mixin(SignBlockEntityRenderer.class)
@EligibleIf(configEnabled="*.legible_signs", envMatches=Env.CLIENT)
public class MixinSignBlockEntityRenderer {

	@Unique
	private static final String RENDER = "render(Lnet/minecraft/block/entity/SignBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V";
	
	@Unique
	private SignBlockEntity fabrication$currentEntity;
	
	@Inject(at=@At("HEAD"), method=RENDER)
	public void renderHead(SignBlockEntity sbe, float f, MatrixStack matrices, VertexConsumerProvider vcp, int i, int j, CallbackInfo ci) {
		fabrication$currentEntity = sbe;
	}
	
	@Inject(at=@At("TAIL"), method=RENDER)
	public void renderTail(SignBlockEntity sbe, float f, MatrixStack matrices, VertexConsumerProvider vcp, int i, int j, CallbackInfo ci) {
		fabrication$currentEntity = null;
	}
	
	@ModifyArg(at=@At(value="INVOKE", target="net/minecraft/client/font/TextRenderer.draw(Lnet/minecraft/text/OrderedText;FFIZLnet/minecraft/util/math/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;ZII)I"),
			method=RENDER, index=3)
	public int modifySignTextColor(int orig) {
		if (!RuntimeChecks.check("*.legible_signs")) return orig;
		DyeColor dc = fabrication$currentEntity.getTextColor();
		switch (dc) {
			case BLACK: return 0x000000;
			case GRAY: return 0x333333;
			case BROWN: return dc.getSignColor();
			default: return FabRefl.getColor(dc);
		}
	}
	
}
