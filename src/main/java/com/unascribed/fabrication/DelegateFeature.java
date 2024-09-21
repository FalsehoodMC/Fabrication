package com.unascribed.fabrication;

import com.unascribed.fabrication.support.Feature;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class DelegateFeature implements Feature {

	private final Feature delegate;

	public DelegateFeature(String className) {
		try {
			delegate = (Feature)Class.forName(className).getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void apply(MinecraftServer minecraftServer, World world) {
		delegate.apply(minecraftServer, world);
	}

	@Override
	public boolean undo(MinecraftServer minecraftServer, World world) {
		return delegate.undo(minecraftServer, world);
	}

	@Override
	public String getConfigKey() {
		return delegate.getConfigKey();
	}

}
