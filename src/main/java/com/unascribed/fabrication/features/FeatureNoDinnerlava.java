package com.unascribed.fabrication.features;

import java.util.Set;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.ConfiguredFeatures.Configs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

@EligibleIf(configAvailable="*.no_dinnerlava")
public class FeatureNoDinnerlava implements Feature {

	private Set<Block> originalValidBlocks;

	@Override
	public void apply(MinecraftServer minecraftServer, World world) {
		originalValidBlocks = Configs.ENCLOSED_NETHER_SPRING_CONFIG.validBlocks;
		Configs.ENCLOSED_NETHER_SPRING_CONFIG.validBlocks = ImmutableSet.of();
	}

	@Override
	public boolean undo(MinecraftServer minecraftServer, World world) {
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
