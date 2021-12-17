package com.unascribed.fabrication.mixin.g_weird_tweaks.encroaching_emeralds;

import com.unascribed.fabrication.interfaces.GenerationSettingsBuilderContains;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.world.biome.GenerationSettings;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GenerationSettings.Builder.class)
@EligibleIf(configAvailable="*.encroaching_emeralds")
public abstract class MixinGenerationSettingsBuilder implements GenerationSettingsBuilderContains {
	private boolean fabrication$hasAddedEmeralds = false;

	@Override
	public boolean fabrication$hasEmeralds() {
		return fabrication$hasAddedEmeralds;
	}

	@Override
	public void fabrication$setEmeralds() {
		fabrication$hasAddedEmeralds = true;
	}
}
