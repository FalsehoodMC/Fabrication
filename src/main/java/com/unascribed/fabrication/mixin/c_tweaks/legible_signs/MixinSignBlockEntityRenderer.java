package com.unascribed.fabrication.mixin.c_tweaks.legible_signs;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SignBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.legible_signs", envMatches=Env.CLIENT)
public class MixinSignBlockEntityRenderer {

	@Hijack(method="render(Lnet/minecraft/block/entity/SignBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
			target="Lnet/minecraft/util/DyeColor;getSignColor()I")
	private HijackReturn modifySignTextColor(DyeColor dc) {
		if (FabConf.isEnabled("*.legible_signs")){
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
			return new HijackReturn(res);
		}
		return null;
	}

}
