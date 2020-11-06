package com.unascribed.fabrication.features;

import com.unascribed.fabrication.ResourcePackFeature;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

@EligibleIf(configEnabled="*.oak_is_apple", envMatches=Env.CLIENT, modLoaded="fabric")
public class FeatureOakIsApple extends ResourcePackFeature {

	public FeatureOakIsApple() {
		super("oak_is_apple");
	}
	
}
