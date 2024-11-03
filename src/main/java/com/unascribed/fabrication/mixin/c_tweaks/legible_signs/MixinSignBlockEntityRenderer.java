package com.unascribed.fabrication.mixin.c_tweaks.legible_signs;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.block.entity.SignText;
import net.minecraft.util.math.ColorHelper;
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
@FailOn(invertedSpecialConditions={SpecialEligibility.FORGE, SpecialEligibility.NOT_FORGE})
public class MixinSignBlockEntityRenderer {

	@FabInject(at=@At("HEAD"), method="getColor(Lnet/minecraft/block/entity/SignText;)I", cancellable = true)
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
					// TODO?
					int rgb = dc.getSignColor();
					res = Math.round(ColorHelper.Argb.getGreen(rgb)*255.0F) << 16 | Math.round(ColorHelper.Argb.getBlue(rgb)*255.0F) << 8 | Math.round(ColorHelper.Argb.getRed(rgb)*255);
				}
			}
			cir.setReturnValue(res);
		}
	}
}
