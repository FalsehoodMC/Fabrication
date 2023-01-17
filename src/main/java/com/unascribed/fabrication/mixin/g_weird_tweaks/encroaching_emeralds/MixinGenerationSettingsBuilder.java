package com.unascribed.fabrication.mixin.g_weird_tweaks.encroaching_emeralds;

import com.unascribed.fabrication.interfaces.GenerationSettingsAddEmeralds;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.OrePlacedFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
/*
@Mixin(GenerationSettings.Builder.class)
@EligibleIf(configAvailable="*.encroaching_emeralds")
public abstract class MixinGenerationSettingsBuilder implements GenerationSettingsAddEmeralds {
	@Shadow @Final
	private List<List<RegistryEntry<PlacedFeature>>> features;

	private boolean fabrication$hasDefaultOres = false;

	@Override
	public void fabrication$addEmeralds() {
		fabrication$hasDefaultOres = true;
	}

	@FabInject(at=@At("HEAD"), method="build()Lnet/minecraft/world/biome/GenerationSettings;")
	public void build(CallbackInfoReturnable<GenerationSettings> cir) {
		if (!fabrication$hasDefaultOres) return;
		int step = GenerationStep.Feature.UNDERGROUND_ORES.ordinal();
		if (features.size() > step && !features.get(step).contains(OrePlacedFeatures.ORE_EMERALD)) {
			features.get(step).add(OrePlacedFeatures.ORE_EMERALD);
		}
	}
}
*/
