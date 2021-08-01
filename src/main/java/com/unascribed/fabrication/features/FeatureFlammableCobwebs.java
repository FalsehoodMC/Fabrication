package com.unascribed.fabrication.features;

import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;

@EligibleIf(configAvailable="*.flammable_cobwebs")
public class FeatureFlammableCobwebs implements Feature {

	@Override
	public void apply() {
		FabRefl.FireBlock_registerFlammableBlock((FireBlock)Blocks.FIRE, Blocks.COBWEB, 60, 100);
	}
	
	@Override
	public boolean undo() {
		FabRefl.getBurnChances((FireBlock)Blocks.FIRE).remove(Blocks.COBWEB);
		FabRefl.getSpreadChances((FireBlock)Blocks.FIRE).remove(Blocks.COBWEB);
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.flammable_cobwebs";
	}

}
