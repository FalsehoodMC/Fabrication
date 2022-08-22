package com.unascribed.fabrication.mixin.b_utility.enter_selects_highlighted_suggestion;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
@EligibleIf(configAvailable="*.old_sheep_shear")
public interface AccessorSuggestionWindow {

	@Accessor("completed")
	boolean fabrication$getCompleated();

}
