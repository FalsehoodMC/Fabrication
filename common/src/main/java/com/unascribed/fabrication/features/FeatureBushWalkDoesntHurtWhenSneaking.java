package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.EitherPredicateFeature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@EligibleIf(configAvailable="*.bush_walk_doesnt_hurt_when_sneaking")
public class FeatureBushWalkDoesntHurtWhenSneaking extends EitherPredicateFeature<LivingEntity> {

	@Override
	public String getConfigKey() {
		return "*.bush_walk_doesnt_hurt_when_sneaking";
	}

	public FeatureBushWalkDoesntHurtWhenSneaking() {
		super("*.bush_walk_doesnt_hurt", Entity::isSneaky);
	}
}
