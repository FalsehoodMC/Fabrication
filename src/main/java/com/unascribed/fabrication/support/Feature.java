package com.unascribed.fabrication.support;

import net.minecraft.world.World;

public interface Feature {
	void apply(World world);
	boolean undo(World world);
	String getConfigKey();
}
