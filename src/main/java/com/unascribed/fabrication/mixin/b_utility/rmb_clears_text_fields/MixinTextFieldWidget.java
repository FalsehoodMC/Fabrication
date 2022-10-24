package com.unascribed.fabrication.mixin.b_utility.rmb_clears_text_fields;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextFieldWidget.class)
@EligibleIf(configAvailable="*.rmb_clears_text_fields", envMatches=Env.CLIENT)
public abstract class MixinTextFieldWidget extends ClickableWidget {

	@Shadow
	private boolean focusUnlocked;

	@Shadow
	public abstract boolean isVisible();

	@Shadow
	protected abstract boolean isEditable();

	@Shadow
	public abstract void setText(String text);

	public MixinTextFieldWidget(int x, int y, int width, int height, Text message) {
		super(x, y, width, height, message);
	}

	@FabInject(at=@At(value="HEAD"), method="mouseClicked(DDI)Z", cancellable=true)
	public void rmbClear(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.rmb_clears_text_fields")) return;
		if (button == 1 && this.isVisible() && this.isEditable() && (this.isFocused() || focusUnlocked) && mouseX >= this.x && mouseX < (this.x + this.width) && mouseY >= this.y && mouseY < (this.y + this.height)) {
			this.setText("");
			cir.setReturnValue(true);
		}
	}

}
