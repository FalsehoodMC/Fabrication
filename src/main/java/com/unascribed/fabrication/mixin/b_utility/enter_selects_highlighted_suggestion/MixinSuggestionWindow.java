package com.unascribed.fabrication.mixin.b_utility.enter_selects_highlighted_suggestion;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets="net.minecraft.client.gui.screen.CommandSuggestor$SuggestionWindow")
@EligibleIf(configAvailable="*.enter_selects_highlighted_suggestion", envMatches=Env.CLIENT)
public abstract class MixinSuggestionWindow {

	@Shadow
	public abstract void complete();

	@Shadow
	private boolean completed;

	@Inject(at=@At(value="HEAD"), method="keyPressed(III)Z", cancellable=true)
	public void onStoppedUsing(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (!MixinConfigPlugin.isEnabled("*.enter_selects_highlighted_suggestion")) return;
		if ((keyCode == 257 || keyCode == 335) && !this.completed){
			this.complete();
			cir.setReturnValue(true);
		}
	}


}
