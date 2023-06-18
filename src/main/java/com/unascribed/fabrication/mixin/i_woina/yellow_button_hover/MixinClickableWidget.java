package com.unascribed.fabrication.mixin.i_woina.yellow_button_hover;

import com.unascribed.fabrication.FabConf;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyArg;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.gui.widget.ClickableWidget;

@Mixin(PressableWidget.class)
@EligibleIf(configAvailable="*.yellow_button_hover", envMatches=Env.CLIENT)
public abstract class MixinClickableWidget extends ClickableWidget {

	public MixinClickableWidget(int x, int y, int width, int height, Text message) {
		super(x, y, width, height, message);
	}

	@FabModifyArg(method="renderButton(Lnet/minecraft/client/gui/DrawContext;IIF)V", index=2,
			at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/widget/PressableWidget;drawMessage(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;I)V"))
	private int yellowText(int old) {
		if(FabConf.isEnabled("*.yellow_button_hover") && this.isHovered() && this.active) return 0xFFFFA0 | (old & 0xFF000000);
		return old;
	}
}
