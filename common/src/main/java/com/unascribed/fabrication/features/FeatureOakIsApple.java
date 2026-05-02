package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.feature.ResourcePackFeature;

@EligibleIf(configAvailable="*.oak_is_apple", envMatches=Env.CLIENT)
public class FeatureOakIsApple extends ResourcePackFeature {

	public FeatureOakIsApple() {
		super("oak_is_apple");
	}

}
