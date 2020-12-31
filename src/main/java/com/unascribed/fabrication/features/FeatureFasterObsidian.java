package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.ImmutableList;

import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;

@EligibleIf(configEnabled="*.faster_obsidian")
public class FeatureFasterObsidian implements Feature {

	private final ImmutableList<Block> BLOCKS = ImmutableList.of(
			Blocks.OBSIDIAN,
			Blocks.CRYING_OBSIDIAN,
			Blocks.ENDER_CHEST
		);
	
	@Override
	public void apply() {
		amendHardness(1/3f);
	}
	
	@Override
	public boolean undo() {
		amendHardness(3);
		return true;
	}

	private void amendHardness(float m) {
		for (Block b : BLOCKS) {
			for (BlockState bs : b.getStateManager().getStates()) {
				try {
					float base = FabRefl.getHardness(bs);
					float nw = base*m;
					FabRefl.setHardness(bs, nw);
				} catch (Exception e) {
					throw new RuntimeException("Can't update hardness", e);
				}
			}
		}
	}

	@Override
	public String getConfigKey() {
		return "*.faster_obsidian";
	}

}
