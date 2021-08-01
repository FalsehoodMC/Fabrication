package com.unascribed.fabrication.features;

import com.unascribed.fabrication.DelegateFeature;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;

@EligibleIf(configAvailable="*.mods_command", specialConditions=SpecialEligibility.FORGE)
public class FeatureModsCommandForge extends DelegateFeature {

	public FeatureModsCommandForge() {
		super("com.unascribed.fabrication.FeatureModsCommandForgeImpl");
	}

}
