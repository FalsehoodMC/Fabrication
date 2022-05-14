package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.EitherPredicateFeature;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;

@EligibleIf(configAvailable="*.creepers_cant_climb")
public class FeatureCreepersCantClimbLadders extends EitherPredicateFeature<LivingEntity> {

	@Override
	public String getConfigKey() {
		return "*.creepers_cant_climb";
	}

	public FeatureCreepersCantClimbLadders() {
		super("*.creepers_cant_climb",
				livingEntity -> livingEntity instanceof CreeperEntity
		);
	}
}
