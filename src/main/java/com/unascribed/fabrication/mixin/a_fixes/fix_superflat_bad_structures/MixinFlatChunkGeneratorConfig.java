package com.unascribed.fabrication.mixin.a_fixes.fix_superflat_bad_structures;

import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;

@Mixin(FlatChunkGeneratorConfig.class)
@EligibleIf(configAvailable="*.fix_superflat_bad_structures")
public class MixinFlatChunkGeneratorConfig {

	@Shadow @Final
	private Registry<Biome> biomeRegistry;

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/world/biome/GenerationSettings$Builder.feature (Lnet/minecraft/world/gen/GenerationStep$Feature;Lnet/minecraft/util/registry/RegistryEntry;)Lnet/minecraft/world/biome/GenerationSettings$Builder;"),
			method="createBiome()Lnet/minecraft/world/biome/Biome;")
	public GenerationSettings.Builder createBiomeAddStructureFeature(GenerationSettings.Builder subject, GenerationStep.Feature featureStep, RegistryEntry<PlacedFeature> feature) {
		if (MixinConfigPlugin.isEnabled("*.fix_superflat_bad_structures") && feature == null) {
			FabLog.debug("Preventing a bad structure from being added to a flat world generator.");
		} else {
			subject.feature(featureStep, feature);
		}
		return subject;
	}

}
