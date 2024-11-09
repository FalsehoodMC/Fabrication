package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import net.minecraft.block.Block;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.gen.feature.NetherConfiguredFeatures;

@EligibleIf(configAvailable="*.no_dinnerlava")
public class FeatureNoDinnerlava implements Feature {

	private RegistryEntryList<Block> originalValidBlocks;

	@Override
	public void apply() {
		originalValidBlocks = NetherConfiguredFeatures.SPRING_NETHER_CLOSED.value().config().validBlocks;
		NetherConfiguredFeatures.SPRING_NETHER_CLOSED.value().config().validBlocks = RegistryEntryList.of();
	}

	@Override
	public boolean undo() {
		if (originalValidBlocks != null) {
			NetherConfiguredFeatures.SPRING_NETHER_CLOSED.value().config().validBlocks = originalValidBlocks;
			originalValidBlocks = null;
		}
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.no_dinnerlava";
	}

}
