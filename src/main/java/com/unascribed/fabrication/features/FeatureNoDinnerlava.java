package com.unascribed.fabrication.features;

import java.util.Set;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.ConfiguredFeatures.Configs;

@EligibleIf(configEnabled="*.no_dinnerlava")
public class FeatureNoDinnerlava implements Feature {

	private Set<Block> originalValidBlocks;

	@Override
	public void apply() {
		originalValidBlocks = Configs.ENCLOSED_NETHER_SPRING_CONFIG.validBlocks;
		Configs.ENCLOSED_NETHER_SPRING_CONFIG.validBlocks = ImmutableSet.of();
	}

	@Override
	public boolean undo() {
		if (originalValidBlocks != null) {
			Configs.ENCLOSED_NETHER_SPRING_CONFIG.validBlocks = originalValidBlocks;
			originalValidBlocks = null;
		}
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.no_dinnerlava";
	}

}
