package com.unascribed.fabrication.features;

import java.util.Set;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.NetherConfiguredFeatures;

@EligibleIf(configAvailable="*.no_dinnerlava")
public class FeatureNoDinnerlava implements Feature {

	private Set<Block> originalValidBlocks;

	@Override
	public void apply() {
		originalValidBlocks = NetherConfiguredFeatures.SPRING_NETHER_CLOSED.config.validBlocks;
		NetherConfiguredFeatures.SPRING_NETHER_CLOSED.config.validBlocks = ImmutableSet.of();
	}

	@Override
	public boolean undo() {
		if (originalValidBlocks != null) {
			NetherConfiguredFeatures.SPRING_NETHER_CLOSED.config.validBlocks = originalValidBlocks;
			originalValidBlocks = null;
		}
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.no_dinnerlava";
	}

}
