package com.unascribed.fabrication.mixin.a_fixes.fix_superflat_bad_structures;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.injection.UnnamedMagic;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(FlatChunkGeneratorConfig.class)
@EligibleIf(configAvailable="*.fix_superflat_bad_structures")
public class MixinFlatChunkGeneratorConfig {

	@UnnamedMagic(target={"net/minecraft/world/biome/GenerationSettings$Builder.structureFeature(Lnet/minecraft/world/gen/feature/ConfiguredStructureFeature;)Lnet/minecraft/world/biome/GenerationSettings$Builder;", "Lnet/minecraft/class_5485$class_5495;method_30995(Lnet/minecraft/class_5312;)Lnet/minecraft/class_5485$class_5495;"},
			method={"createBiome()Lnet/minecraft/world/biome/Biome;", "method_28917()Lnet/minecraft/class_1959;"})
	private static Optional fabrication$errorCheckStructureFeature(GenerationSettings.Builder subject, ConfiguredStructureFeature<?, ?> feature) {
		if (MixinConfigPlugin.isEnabled("*.fix_superflat_bad_structures") && feature == null) {
			FabLog.debug("Preventing a bad structure from being added to a flat world generator.");
			return Optional.of(null);
		}
		return Optional.empty();
	}

}
