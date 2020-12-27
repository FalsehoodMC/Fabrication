package com.unascribed.fabrication;

import com.unascribed.fabrication.support.Feature;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;

public class FeatureAnvilDamageOnlyOnFallForgeImpl implements Feature {

	private boolean registered = false;
	private boolean applied = false;
	
	public void onAnvilRepair(AnvilRepairEvent e) {
		if (applied) {
			e.setBreakChance(0);
		}
	}
	
	@Override
	public void apply() {
		applied = true;
		if (!registered) {
			registered = true;
			MinecraftForge.EVENT_BUS.addListener(this::onAnvilRepair);
		}
	}

	@Override
	public boolean undo() {
		applied = false;
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.anvil_damage_only_on_fall";
	}

}
