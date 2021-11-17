package com.unascribed.fabrication.mixin.i_woina.yellow_button_hover;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClickableWidget.class)
@EligibleIf(configAvailable="*.yellow_button_hover", envMatches=Env.CLIENT)
public abstract class MixinClickableWidget {

    @Shadow public abstract boolean isHovered();

    @Shadow public boolean active;

    @ModifyArg(method="renderButton(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", index=5,
            at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/widget/ClickableWidget;drawCenteredText(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"))
    private int yellowText(int old) {
        if(MixinConfigPlugin.isEnabled("*.yellow_button_hover") && this.isHovered() && this.active) return 0xffffffa0;
        return old;
    }
}