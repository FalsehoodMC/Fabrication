package com.unascribed.fabrication;

import com.unascribed.fabrication.support.Feature;
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
	public void apply(World world) {
		delegate.apply(world);
	}

	@Override
	public boolean undo(World world) {
		return delegate.undo(world);
	}

	@Override
	public String getConfigKey() {
		return delegate.getConfigKey();
	}

}
