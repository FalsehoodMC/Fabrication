package com.unascribed.fabrication.interfaces;

import net.minecraft.world.gen.feature.PlacedFeature;

public interface GenerationSettingsBuilderContains {
	boolean fabrication$builderContains(net.minecraft.world.gen.GenerationStep.Feature featureStep, PlacedFeature feature);
}
