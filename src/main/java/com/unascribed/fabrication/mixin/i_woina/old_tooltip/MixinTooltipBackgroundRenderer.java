package com.unascribed.fabrication.mixin.i_woina.old_tooltip;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TooltipBackgroundRenderer.class)
@EligibleIf(configAvailable="*.old_tooltip", envMatches=Env.CLIENT)
public abstract class MixinTooltipBackgroundRenderer {

	@FabInject(method="render(Lnet/minecraft/client/gui/DrawContext;IIIII)V", at=@At("HEAD"))
	private static void oldTooltip(DrawContext context, int x, int y, int width, int height, int z, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.old_tooltip")) return;
		int i = x - 3;
		int j = y - 3;
		int k = width + 3 + 3;
		int l = height + 3 + 3;
		context.fillGradient(k - 3, l - 3, k + i + 3, l + height, z, -1073741824, -1073741824);
	}
}
