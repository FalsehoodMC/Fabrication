package com.unascribed.fabrication.mixin.b_utility.enter_selects_highlighted_suggestion;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;


import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;

import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatInputSuggestor.class)
@EligibleIf(configAvailable="*.enter_selects_highlighted_suggestion", envMatches=Env.CLIENT)
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public abstract class MixinSuggestionWindow {

	@Hijack(method="keyPressed(III)Z", target="Lnet/minecraft/client/gui/screen/ChatInputSuggestor$SuggestionWindow;keyPressed(III)Z")
	private static HijackReturn fabrication$enterAcceptsSuggestion(ChatInputSuggestor.SuggestionWindow window, int keyCode, int scanCode, int modifiers) {
		if (!FabConf.isEnabled("*.enter_selects_highlighted_suggestion")) return null;
		if ((keyCode == GLFW_KEY_ENTER || keyCode == GLFW_KEY_KP_ENTER) && window instanceof AccessorSuggestionWindow && !((AccessorSuggestionWindow) window).fabrication$getCompleated()){
			window.complete();
			return HijackReturn.FALSE;
		}
		return null;
	}


}
