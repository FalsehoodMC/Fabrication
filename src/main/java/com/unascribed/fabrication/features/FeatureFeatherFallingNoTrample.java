package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

@EligibleIf(configAvailable="*.feather_falling_no_trample")
public class FeatureFeatherFallingNoTrample implements Feature {
    @Override
    public void apply() {
        ConfigPredicates.put("tweaks.no_trample",
                (Predicate<LivingEntity>) livingEntity -> EnchantmentHelper.getEquipmentLevel(Enchantments.FEATHER_FALLING, livingEntity)>=1
        );
    }

    @Override
    public boolean undo() {
        ConfigPredicates.remove("tweaks.no_trample");
        return true;
    }

    @Override
    public String getConfigKey() {
        return "*.feather_falling_no_trample";
    }
}
