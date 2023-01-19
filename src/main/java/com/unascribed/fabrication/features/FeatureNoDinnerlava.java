package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import net.minecraft.block.Block;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.gen.feature.NetherConfiguredFeatures;
import net.minecraft.world.gen.feature.SpringFeatureConfig;

@EligibleIf(configAvailable="*.no_dinnerlava")
public class FeatureNoDinnerlava implements Feature {

	private RegistryEntryList<Block> originalValidBlocks;

	@Override
	public void apply() {
		SpringFeatureConfig featureConfig = (SpringFeatureConfig) BuiltinRegistries.createWrapperLookup().getWrapperOrThrow(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(NetherConfiguredFeatures.SPRING_NETHER_CLOSED).value().config();
		originalValidBlocks = featureConfig.validBlocks;
		featureConfig.validBlocks = RegistryEntryList.of();
	}

	@Override
	public boolean undo() {
		if (originalValidBlocks != null) {
			((SpringFeatureConfig) BuiltinRegistries.createWrapperLookup().getWrapperOrThrow(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(NetherConfiguredFeatures.SPRING_NETHER_CLOSED).value().config()).validBlocks = originalValidBlocks;
			originalValidBlocks = null;
		}
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.no_dinnerlava";
	}

}
