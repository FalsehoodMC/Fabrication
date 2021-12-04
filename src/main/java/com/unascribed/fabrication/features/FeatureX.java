package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

@EligibleIf(modLoaded="fabricationx")
public class FeatureX implements Feature {

	@Override
	public void apply() {
		try {
			Class.forName("com.unascribed.fabrication.x.FabricationX").getMethod("init").invoke(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean undo() {
		return false;
	}

	@Override
	public String getConfigKey() {
		return null;
	}

}
