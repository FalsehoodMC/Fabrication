package com.unascribed.fabrication.features;

import com.unascribed.fabrication.DelegateFeature;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;

@EligibleIf(configAvailable="*.anvil_damage_only_on_fall", specialConditions=SpecialEligibility.FORGE)
public class FeatureAnvilDamageOnlyOnFallForge extends DelegateFeature {

	public FeatureAnvilDamageOnlyOnFallForge() {
		super("com.unascribed.fabrication.FeatureAnvilDamageOnlyOnFallForgeImpl");
	}

}
