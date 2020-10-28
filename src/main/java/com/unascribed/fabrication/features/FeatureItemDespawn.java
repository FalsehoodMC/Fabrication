package com.unascribed.fabrication.features;

import java.util.Map;

import com.unascribed.fabrication.ParsedTime;
import com.unascribed.fabrication.Resolvable;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.SpecialEligibility;

import com.google.common.collect.Maps;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@EligibleIf(specialConditions=SpecialEligibility.ITEM_DESPAWN_NOT_ALL_UNSET)
public class FeatureItemDespawn implements Feature {

	public static final Map<Resolvable<Item>, ParsedTime> itemDespawns = Maps.newHashMap();
	public static final Map<Resolvable<Enchantment>, ParsedTime> enchDespawns = Maps.newHashMap();
	public static final Map<String, ParsedTime> nbtBools = Maps.newHashMap();
	
	public static ParsedTime curseDespawn = ParsedTime.UNSET;
	public static ParsedTime normalEnchDespawn = ParsedTime.UNSET;
	public static ParsedTime treasureDespawn = ParsedTime.UNSET;
	
	public static ParsedTime defaultDespawn = ParsedTime.UNSET;
	public static ParsedTime dropsDespawn = ParsedTime.UNSET;
	public static ParsedTime playerDeathDespawn = ParsedTime.UNSET;
	
	@Override
	public void apply() {
		parseConfig();
	}

	@Override
	public boolean undo() {
		clearConfig();
		return true;
	}

	@Override
	public String getConfigKey() {
		return null;
	}

	public static void clearConfig() {
		itemDespawns.clear();
		enchDespawns.clear();
		nbtBools.clear();
		curseDespawn = ParsedTime.UNSET;
		normalEnchDespawn = ParsedTime.UNSET;
		treasureDespawn = ParsedTime.UNSET;
		defaultDespawn = ParsedTime.UNSET;
		dropsDespawn = ParsedTime.UNSET;
		playerDeathDespawn = ParsedTime.UNSET;
	}
	
	public static void parseConfig() {
		clearConfig();
		for (Map.Entry<String, String> en : MixinConfigPlugin.rawItemDespawnConfig.entrySet()) {
			System.out.println(en.getKey()+"="+en.getValue());
			ParsedTime time = ParsedTime.parse(en.getValue());
			if (time == ParsedTime.UNSET) continue;
			if (en.getKey().startsWith("@enchantments.")) {
				String id = en.getKey().substring(14);
				if ("@curses".equals(id)) {
					curseDespawn = time;
				} else if ("@normal".equals(id)) {
					normalEnchDespawn = time;
				} else if ("@treasure".equals(id)) {
					treasureDespawn = time;
				} else if (id.startsWith("@")) {
					throw new IllegalArgumentException("Unknown special key "+en.getKey());
				} else {
					enchDespawns.put(Resolvable.of(new Identifier(id), Registry.ENCHANTMENT), time);
				}
			} else if (en.getKey().startsWith("@special.")) {
				String id = en.getKey().substring(9);
				if ("default".equals(id)) {
					defaultDespawn = time;
				} else if ("drops".equals(id)) {
					dropsDespawn = time;
				} else if ("player_death".equals(id)) {
					playerDeathDespawn = time;
				} else {
					throw new IllegalArgumentException("Unknown special key "+en.getKey());
				}
			} else if (en.getKey().startsWith("@nbtbools.")) {
				String key = en.getKey().substring(10);
				nbtBools.put(key, time);
			} else {
				itemDespawns.put(Resolvable.of(new Identifier(en.getKey()), Registry.ITEM), time);
			}
		}
	}

}
