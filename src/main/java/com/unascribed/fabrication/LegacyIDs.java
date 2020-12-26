package com.unascribed.fabrication;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LegacyIDs {

	private static final Int2ObjectOpenHashMap<Item> data = new Int2ObjectOpenHashMap<>();
	
	static {
		try {
			JsonObject obj = new Gson().fromJson(Resources.toString(LegacyIDs.class.getClassLoader().getResource("legacy_ids.json"), Charsets.UTF_8), JsonObject.class);
			for (Map.Entry<String, JsonElement> en : obj.entrySet()) {
				if (en.getKey().equals("_comment")) continue;
				int colon = en.getKey().indexOf(':');
				int id = Integer.parseInt(en.getKey().substring(0, colon));
				int meta = Integer.parseInt(en.getKey().substring(colon+1));
				int key = id << 16 | meta;
				data.put(key, Registry.ITEM.get(new Identifier(en.getValue().getAsString())));
			}
		} catch (Throwable t) {
			FabLog.warn("Failed to load legacy IDs", t);
		}
	}
	
	public static Item lookup(int id, int meta) {
		return data.get(id << 16 | meta);
	}
	
}
