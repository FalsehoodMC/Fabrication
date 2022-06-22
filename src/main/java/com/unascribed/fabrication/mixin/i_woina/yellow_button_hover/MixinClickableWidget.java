package com.unascribed.fabrication.mixin.i_woina.yellow_button_hover;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyArg;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.gui.widget.ClickableWidget;

@Mixin(ClickableWidget.class)
@EligibleIf(configAvailable="*.yellow_button_hover", envMatches=Env.CLIENT)
public abstract class MixinClickableWidget {

	@Shadow public abstract boolean isHovered();

	@Shadow public boolean active;

	@FabModifyArg(method="renderButton(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", index=5,
			at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/widget/ClickableWidget;drawCenteredText(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"))
	private int yellowText(int old) {
		if(FabConf.isEnabled("*.yellow_button_hover") && this.isHovered() && this.active) return 0xFFFFA0 | (old & 0xFF000000);
		return old;
	}
}
