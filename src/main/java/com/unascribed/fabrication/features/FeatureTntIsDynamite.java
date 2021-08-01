package com.unascribed.fabrication.features;

import com.unascribed.fabrication.ResourcePackFeature;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

@EligibleIf(configAvailable="*.tnt_is_dynamite", envMatches=Env.CLIENT)
public class FeatureTntIsDynamite extends ResourcePackFeature {

	public FeatureTntIsDynamite() {
		super("tnt_is_dynamite");
	}
	
}
