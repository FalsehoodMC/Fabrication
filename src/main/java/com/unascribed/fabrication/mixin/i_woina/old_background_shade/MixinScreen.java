package com.unascribed.fabrication.mixin.i_woina.old_background_shade;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabModifyConst;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(Screen.class)
@EligibleIf(configAvailable="*.old_background_shade", envMatches=Env.CLIENT)
public class MixinScreen {

	@FabModifyConst(constant=@Constant(intValue=-1072689136), method="renderBackground(Lnet/minecraft/client/util/math/MatrixStack;)V")
	public int modifyTopBgColor(int color) {
		if (!FabConf.isEnabled("*.old_background_shade")) return color;
		return 0x60050500;
	}

	@FabModifyConst(constant=@Constant(intValue=-804253680), method="renderBackground(Lnet/minecraft/client/util/math/MatrixStack;)V")
	public int modifyBottomBgColor(int color) {
		if (!FabConf.isEnabled("*.old_background_shade")) return color;
		return 0xA0303060;
	}
}
