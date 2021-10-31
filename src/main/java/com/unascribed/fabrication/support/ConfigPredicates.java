package com.unascribed.fabrication.support;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ConfigPredicates {

    public static Map<String, Object> predicates;
    public static final Map<String, Object> predicateDefaults = new HashMap<>();

    public static<T> boolean shouldRun(String configKey, T test) {
        return shouldRun(configKey, test, true);
    }

    public static<T> boolean shouldRun(String configKey, T test, boolean ifMissingReturn) {
        configKey = MixinConfigPlugin.remap(configKey);
        Predicate<T> predicate;
        try{
             predicate= (Predicate<T>) predicates.get(configKey);
            if (predicate == null) return ifMissingReturn;
        }catch (Exception e){
            return ifMissingReturn;
        }
        return predicate.test(test);
    }

    static{
        predicateDefaults.put("tweaks.feather_falling_no_trample",
                (Predicate<LivingEntity>) livingEntity -> EnchantmentHelper.getEquipmentLevel(Enchantments.FEATHER_FALLING, livingEntity)>=1
        );
        predicates = predicateDefaults;
    }
}
