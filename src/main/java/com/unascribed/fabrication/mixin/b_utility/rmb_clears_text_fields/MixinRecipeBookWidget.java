package com.unascribed.fabrication.mixin.b_utility.rmb_clears_text_fields;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookWidget.class)
@EligibleIf(configAvailable="*.rmb_clears_text_fields", envMatches=Env.CLIENT)
public abstract class MixinRecipeBookWidget {

	@Shadow
	protected abstract void refreshSearchResults();

	@FabInject(at=@At(value="RETURN"), method="mouseClicked(DDI)Z")
	public void rmbClear(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.rmb_clears_text_fields")) return;
		if (cir.getReturnValueZ()) {
			this.refreshSearchResults();
		}
	}

}
