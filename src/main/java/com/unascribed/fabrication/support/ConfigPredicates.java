package com.unascribed.fabrication.support;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        predicateDefaults.put("tweaks.bush_walk_doesnt_hurt_with_armor",
                (Predicate<LivingEntity>) livingEntity -> !(
                        livingEntity.getEquippedStack(EquipmentSlot.LEGS).isEmpty()
                        || livingEntity.getEquippedStack(EquipmentSlot.FEET).isEmpty()
                )
        );
        predicateDefaults.put("tweaks.feather_falling_no_trample",
                (Predicate<LivingEntity>) livingEntity ->
                        EnchantmentHelper.getEquipmentLevel(Enchantments.FEATHER_FALLING, livingEntity)>=1
        );
        predicateDefaults.put("tweaks.cactus_walk_doesnt_hurt_with_boots",
                (Predicate<LivingEntity>) livingEntity ->
                        !livingEntity.getEquippedStack(EquipmentSlot.FEET).isEmpty()
        );
        predicateDefaults.put("tweaks.cactus_brush_doesnt_hurt_with_chest",
                (Predicate<LivingEntity>) livingEntity ->
                        !livingEntity.getEquippedStack(EquipmentSlot.CHEST).isEmpty()
        );
        predicateDefaults.put("tweaks.creepers_explode_when_on_fire",
                (Predicate<LivingEntity>) livingEntity ->
                        livingEntity.getFireTicks() > 0 && !livingEntity.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)
        );
        predicateDefaults.put("minor_mechanics.cactus_punching_hurts",
                (Predicate<ServerPlayerEntity>) serverPlayerEntity ->
                        serverPlayerEntity.getMainHandStack().isEmpty()
        );
        predicateDefaults.put("minor_mechanics.feather_falling_five",
                (Predicate<LivingEntity>) livingEntity ->
                        EnchantmentHelper.getLevel(Enchantments.FEATHER_FALLING, livingEntity.getEquippedStack(EquipmentSlot.FEET)) >= 5
        );
        predicateDefaults.put("minor_mechanics.feather_falling_five_damages_boots",
                (Predicate<LivingEntity>) livingEntity ->
                        EnchantmentHelper.getLevel(Enchantments.FEATHER_FALLING, livingEntity.getEquippedStack(EquipmentSlot.FEET)) >= 5
        );

        predicates = predicateDefaults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
