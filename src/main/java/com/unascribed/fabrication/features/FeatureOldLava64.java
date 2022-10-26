package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

@EligibleIf(configAvailable="*.old_lava_x64", envMatches=Env.CLIENT)
public class FeatureOldLava64 extends FeatureOldLava {

	@Override
	public String getConfigKey() {
		return "*.old_lava_x64";
	}

}
