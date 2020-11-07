package com.unascribed.fabrication.loaders;

import java.util.Map;

import com.unascribed.fabrication.ParsedTime;
import com.unascribed.fabrication.Resolvable;
import com.unascribed.fabrication.support.ConfigLoader;
import com.google.common.collect.Maps;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LoaderItemDespawn implements ConfigLoader {

	public static final Map<Resolvable<Item>, ParsedTime> itemDespawns = Maps.newHashMap();
	public static final Map<Resolvable<Enchantment>, ParsedTime> enchDespawns = Maps.newHashMap();
	public static final Map<Identifier, ParsedTime> tagDespawns = Maps.newHashMap();
	public static final Map<String, ParsedTime> nbtBools = Maps.newHashMap();
	
	public static ParsedTime curseDespawn = ParsedTime.UNSET;
	public static ParsedTime normalEnchDespawn = ParsedTime.UNSET;
	public static ParsedTime treasureDespawn = ParsedTime.UNSET;
	
	public static ParsedTime defaultDespawn = ParsedTime.UNSET;
	public static ParsedTime dropsDespawn = ParsedTime.UNSET;
	public static ParsedTime playerDeathDespawn = ParsedTime.UNSET;
	
	@Override
	public String getConfigName() {
		return "item_despawn";
	}
	
	@Override
	public void load(Map<String, String> config) {
		itemDespawns.clear();
		enchDespawns.clear();
		tagDespawns.clear();
		nbtBools.clear();
		curseDespawn = ParsedTime.UNSET;
		normalEnchDespawn = ParsedTime.UNSET;
		treasureDespawn = ParsedTime.UNSET;
		defaultDespawn = ParsedTime.UNSET;
		dropsDespawn = ParsedTime.UNSET;
		playerDeathDespawn = ParsedTime.UNSET;
		for (Map.Entry<String, String> en : config.entrySet()) {
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
			} else if (en.getKey().startsWith("@tags.")) {
				String id = en.getKey().substring(6);
				tagDespawns.put(new Identifier(id), time);
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
