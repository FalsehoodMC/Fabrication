package com.unascribed.fabrication.mixin.b_utility.rmb_clears_text_fields;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextFieldWidget.class)
@EligibleIf(configAvailable="*.rmb_clears_text_fields", envMatches= Env.CLIENT)
public interface AccessorTextFieldWidget {
	@Accessor("focusUnlocked")
	boolean fabrication$clear$getFocusUnlocked();
	@Invoker("isEditable")
	boolean fabrication$clear$isEditable();
}
