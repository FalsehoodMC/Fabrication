package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.feature.ResourcePackFeature;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

@EligibleIf(configAvailable="*.oak_is_apple", envMatches=Env.CLIENT)
public class FeatureOakIsApple extends ResourcePackFeature {

	public FeatureOakIsApple() {
		super("oak_is_apple");
	}

}
