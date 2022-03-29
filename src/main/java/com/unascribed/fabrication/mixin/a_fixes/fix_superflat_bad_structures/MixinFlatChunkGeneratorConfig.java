package com.unascribed.fabrication.mixin.a_fixes.fix_superflat_bad_structures;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FlatChunkGeneratorConfig.class)
@EligibleIf(configAvailable="*.fix_superflat_bad_structures")
public class MixinFlatChunkGeneratorConfig {

	@Hijack(target="Lnet/minecraft/world/biome/GenerationSettings$Builder;structureFeature(Lnet/minecraft/world/gen/feature/ConfiguredStructureFeature;)Lnet/minecraft/world/biome/GenerationSettings$Builder;",
			method="createBiome()Lnet/minecraft/world/biome/Biome;")
	private static HijackReturn fabrication$errorCheckStructureFeature(GenerationSettings.Builder subject, ConfiguredStructureFeature<?, ?> feature) {
		if (FabConf.isEnabled("*.fix_superflat_bad_structures") && feature == null) {
			FabLog.debug("Preventing a bad structure from being added to a flat world generator.");
			return new HijackReturn(null);
		}
		return null;
	}

}
