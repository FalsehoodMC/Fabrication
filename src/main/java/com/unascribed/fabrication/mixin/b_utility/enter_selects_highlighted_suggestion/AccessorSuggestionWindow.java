package com.unascribed.fabrication.mixin.b_utility.enter_selects_highlighted_suggestion;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
@EligibleIf(configAvailable="*.enter_selects_highlighted_suggestion", envMatches=Env.CLIENT)
public interface AccessorSuggestionWindow {

	@Accessor("completed")
	boolean fabrication$getCompleated();

}
