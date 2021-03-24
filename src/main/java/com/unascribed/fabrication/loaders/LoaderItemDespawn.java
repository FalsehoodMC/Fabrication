package com.unascribed.fabrication.loaders;

import java.nio.file.Path;
import java.util.Map;

import com.unascribed.fabrication.ParsedTime;
import com.unascribed.fabrication.QDIni;
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
	public static ParsedTime renamedDespawn = ParsedTime.UNSET;
	public static ParsedTime playerDeathDespawn = ParsedTime.UNSET;
	
	@Override
	public String getConfigName() {
		return "item_despawn";
	}
	
	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		itemDespawns.clear();
		enchDespawns.clear();
		tagDespawns.clear();
		nbtBools.clear();
		curseDespawn = ParsedTime.getFrom(config, "@enchantments.@curses");
		normalEnchDespawn = ParsedTime.getFrom(config, "@enchantments.@normal");
		treasureDespawn = ParsedTime.getFrom(config, "@enchantments.@treasure");
		defaultDespawn = ParsedTime.getFrom(config, "@special.default");
		dropsDespawn = ParsedTime.getFrom(config, "@special.drops");
		renamedDespawn = ParsedTime.getFrom(config, "@special.renamed");
		playerDeathDespawn = ParsedTime.getFrom(config, "@special.player_death");
		for (String k : config.keySet()) {
			ParsedTime time = ParsedTime.getFrom(config, k);
			if (time == ParsedTime.UNSET) continue;
			if (k.startsWith("@enchantments.")) {
				String id = k.substring(14);
				if (!id.startsWith("@")) {
					enchDespawns.put(Resolvable.of(new Identifier(id), Registry.ENCHANTMENT), time);
				}
			} else if (k.startsWith("@tags.")) {
				String id = k.substring(6);
				tagDespawns.put(new Identifier(id), time);
			} else if (k.startsWith("@nbtbools.")) {
				String key = k.substring(10);
				nbtBools.put(key, time);
			} else if (!k.startsWith("@")) {
				itemDespawns.put(Resolvable.of(new Identifier(k), Registry.ITEM), time);
			}
		}
	}
	
}
