package com.unascribed.fabrication.mixin.g_weird_tweaks.encroaching_emeralds;

import com.unascribed.fabrication.interfaces.GenerationSettingsBuilderContains;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Supplier;

@Mixin(GenerationSettings.Builder.class)
@EligibleIf(configAvailable="*.encroaching_emeralds")
public abstract class MixinGenerationSettingsBuilder implements GenerationSettingsBuilderContains {
	@Shadow
	@Final
	private List<List<Supplier<PlacedFeature>>> features;

	@Override
	public boolean fabrication$builderContains(net.minecraft.world.gen.GenerationStep.Feature featureStep, PlacedFeature feature) {
		return features.get(featureStep.ordinal()).stream().anyMatch(f -> feature.equals(f.get()));
	}
}
