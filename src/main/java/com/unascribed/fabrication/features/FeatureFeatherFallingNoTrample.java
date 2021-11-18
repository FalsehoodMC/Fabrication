package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.feature.SimplePredicateFeature;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

@EligibleIf(configAvailable="*.feather_falling_no_trample")
public class FeatureFeatherFallingNoTrample extends SimplePredicateFeature {

	@Override
	public String getConfigKey() {
		return "*.feather_falling_no_trample";
	}

	public FeatureFeatherFallingNoTrample() {
		super("*.no_trample",
				(Predicate<LivingEntity>) livingEntity -> EnchantmentHelper.getEquipmentLevel(Enchantments.FEATHER_FALLING, livingEntity)>=1
		);
	}
}
