package com.unascribed.fabrication.mixin.c_tweaks.legible_signs;

import com.unascribed.fabrication.FabConf;
import net.minecraft.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.util.DyeColor;

@Mixin(SignBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.legible_signs", envMatches=Env.CLIENT)
public class MixinSignBlockEntityRenderer {

	@FabInject(at=@At("HEAD"), method= "getColor(Lnet/minecraft/block/entity/SignText;)I", cancellable = true)
	private static void modifySignTextColor(SignText sign, CallbackInfoReturnable<Integer> cir) {
		if (FabConf.isEnabled("*.legible_signs") && !sign.isGlowing()){
			DyeColor dc = sign.getColor();

			int res;
			switch (dc) {
				case BLACK:
					res = 0x000000;
					break;
				case GRAY:
					res = 0x333333;
					break;
				case BROWN:
					res = dc.getSignColor();
					break;
				default: {
					float[] bgr = dc.getColorComponents();
					res = Math.round(bgr[0]*255.0F) << 16 | Math.round(bgr[1]*255.0F) << 8 | Math.round(bgr[2]*255);
				}
			}
			cir.setReturnValue(res);
		}
	}
}
