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

	@FabInject(method="render(Lnet/minecraft/client/gui/DrawContext;IIIII)V", at=@At("HEAD"), cancellable=true)
	private static void oldTooltip(DrawContext context, int x, int y, int width, int height, int z, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.old_tooltip")) return;
		context.fillGradient(x-3, y-3, x+width+3, y+height+3, z, -1073741824, -1073741824);
		ci.cancel();
	}
}
