package com.unascribed.fabrication.support;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public interface Feature {
	void apply(MinecraftServer minecraftServer, World world);
	boolean undo(MinecraftServer minecraftServer, World world);
	String getConfigKey();
}
