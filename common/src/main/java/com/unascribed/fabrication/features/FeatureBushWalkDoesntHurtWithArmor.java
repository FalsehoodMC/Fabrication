package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.EitherPredicateFeature;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

@EligibleIf(configAvailable="*.bush_walk_doesnt_hurt_with_armor")
public class FeatureBushWalkDoesntHurtWithArmor extends EitherPredicateFeature<LivingEntity> {

	@Override
	public String getConfigKey() {
		return "*.bush_walk_doesnt_hurt_with_armor";
	}

	public FeatureBushWalkDoesntHurtWithArmor() {
		super("*.bush_walk_doesnt_hurt",
				livingEntity -> !(livingEntity.getEquippedStack(EquipmentSlot.LEGS).isEmpty() || livingEntity.getEquippedStack(EquipmentSlot.FEET).isEmpty())
		);
	}
}
