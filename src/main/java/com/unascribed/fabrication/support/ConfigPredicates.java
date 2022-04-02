package com.unascribed.fabrication.support;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.unascribed.fabrication.FabConf.remap;

public class ConfigPredicates {

	private static Map<String, Object> active;
	private static Map<String, Feature> idle = new HashMap<>();
	public static final ImmutableMap<String, Object> defaults;

	public static<T> boolean shouldRun(String configKey, T test) {
		return shouldRun(configKey, test, true);
	}

	public static<T> boolean shouldRun(String configKey, T test, boolean defaultValue) {
		configKey = remap(configKey);
		Predicate<T> predicate;
		try{
			predicate = (Predicate<T>) active.get(configKey);
			if (predicate == null) return defaultValue;
		}catch (Exception e){
			return defaultValue;
		}
		return predicate.test(test);
	}

	public static void put(String configKey, Predicate<?> predicate){
		put(configKey, predicate, 0);
	}

	public static void put(String configKey, Predicate<?> predicate, int level){
		configKey = remap(configKey);
		if (!idle.containsKey(configKey)) {
			idle.put(configKey, new Feature());
		}
		if(idle.get(configKey).add(predicate, level)) {
			active.put(configKey, predicate);
		}
	}
	public static void remove(String configKey){
		remove(configKey, 0);
	}
	public static void remove(String configKey, int level){
		configKey = remap(configKey);
		if (idle.containsKey(configKey)){
			Object rtrn = idle.get(configKey).remove(level, active);
			active.put(configKey, rtrn == null ? defaults.get(configKey) : rtrn);
		}
	}

	static{
		Map<String, Object> defaultsMap = new HashMap<>();
		defaultsMap.put(remap("*.cactus_walk_doesnt_hurt_with_boots"),
				(Predicate<LivingEntity>) livingEntity ->
		!livingEntity.getEquippedStack(EquipmentSlot.FEET).isEmpty()
				);
		defaultsMap.put(remap("*.cactus_brush_doesnt_hurt_with_chest"),
				(Predicate<LivingEntity>) livingEntity ->
		!livingEntity.getEquippedStack(EquipmentSlot.CHEST).isEmpty()
				);
		defaultsMap.put(remap("*.creepers_explode_when_on_fire"),
				(Predicate<LivingEntity>) livingEntity ->
		livingEntity.getFireTicks() > 0 && !livingEntity.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)
				);
		defaultsMap.put(remap("*.cactus_punching_hurts"),
				(Predicate<ServerPlayerEntity>) serverPlayerEntity ->
		serverPlayerEntity.getMainHandStack().isEmpty()
				);
		defaultsMap.put(remap("*.feather_falling_five"),
				(Predicate<LivingEntity>) livingEntity ->
		EnchantmentHelper.getLevel(Enchantments.FEATHER_FALLING, livingEntity.getEquippedStack(EquipmentSlot.FEET)) >= 5
				);
		defaultsMap.put(remap("*.feather_falling_five_damages_boots"),
				(Predicate<LivingEntity>) livingEntity ->
		EnchantmentHelper.getLevel(Enchantments.FEATHER_FALLING, livingEntity.getEquippedStack(EquipmentSlot.FEET)) >= 5
				);
		defaults = ImmutableMap.copyOf(defaultsMap);
		active = defaults.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private static class Feature {
		Integer i = Integer.MIN_VALUE;
		Map<Integer, Object> map = new HashMap<>();

		public boolean add(Predicate<?> predicate, int level){
			map.put(level, predicate);
			if (i<=level) {
				i = level;
				return true;
			}
			return false;
		}
		public Object remove(int level, Object defaultVal){
			map.remove(level);
			if (map.isEmpty()) {
				i = Integer.MIN_VALUE;
				return null;
			} else if (i<=level) {
				i = map.keySet().stream().max(Comparator.comparingInt(i -> i)).get();
				return map.get(i);
			}
			return defaultVal;
		}
	}
}
