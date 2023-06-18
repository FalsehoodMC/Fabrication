package com.unascribed.fabrication.mixin.b_utility.rmb_clears_text_fields;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//Should mixin TextFieldWidget but 1.20 removed the mouseCliked method
@Mixin(ClickableWidget.class)
@EligibleIf(configAvailable="*.rmb_clears_text_fields", envMatches=Env.CLIENT)
public abstract class MixinClickableWidget {

	@FabInject(at=@At(value="HEAD"), method="mouseClicked(DDI)Z", cancellable=true)
	public void rmbClear(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (!(this instanceof AccessorTextFieldWidget)) return;
		TextFieldWidget self = (TextFieldWidget)(Object) this;
		if (!FabConf.isEnabled("*.rmb_clears_text_fields")) return;
		if (button == 1 && self.isVisible() && ((AccessorTextFieldWidget)self).fabrication$clear$isEditable() && (self.isFocused() || ((AccessorTextFieldWidget)self).fabrication$clear$getFocusUnlocked()) && mouseX >= self.getX() && mouseX < (self.getX() + self.getWidth()) && mouseY >= self.getY() && mouseY < (self.getY() + self.getHeight())) {
			self.setText("");
			cir.setReturnValue(true);
		}
	}

}
