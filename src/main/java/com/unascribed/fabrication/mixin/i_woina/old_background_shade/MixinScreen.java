package com.unascribed.fabrication.mixin.i_woina.old_background_shade;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screen.class)
@EligibleIf(configAvailable="*.old_background_shade", envMatches=Env.CLIENT)
public class MixinScreen {

	@FabModifyArg(method="renderInGameBackground(Lnet/minecraft/client/gui/DrawContext;)V", index=4, at= @At(value= "INVOKE", target="Lnet/minecraft/client/gui/DrawContext;fillGradient(IIIIII)V"))
	public int modifyTopBgColor(int color) {
		if (!FabConf.isEnabled("*.old_background_shade")) return color;
		if (color != -1072689136) return color;
		return 0x60050500;
	}

	@FabModifyArg(method="renderInGameBackground(Lnet/minecraft/client/gui/DrawContext;)V", index=5, at= @At(value= "INVOKE", target="Lnet/minecraft/client/gui/DrawContext;fillGradient(IIIIII)V"))
	public int modifyBottomBgColor(int color) {
		if (!FabConf.isEnabled("*.old_background_shade")) return color;
		if (color != -804253680) return color;
		return 0xA0303060;
	}
}
