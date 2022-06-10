package com.unascribed.fabrication.logic;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.unascribed.fabrication.FabLog;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LegacyIDs {

	private static final Int2ObjectOpenHashMap<Item> data = new Int2ObjectOpenHashMap<>();
	private static final Int2ObjectOpenHashMap<Identifier> data_id = new Int2ObjectOpenHashMap<>();

	static {
		try {
			JsonObject obj = new Gson().fromJson(Resources.toString(LegacyIDs.class.getClassLoader().getResource("legacy_ids.json"), Charsets.UTF_8), JsonObject.class);
			for (Map.Entry<String, JsonElement> en : obj.entrySet()) {
				if (en.getKey().equals("_comment")) continue;
				int colon = en.getKey().indexOf(':');
				int id = Integer.parseInt(en.getKey().substring(0, colon));
				int meta = Integer.parseInt(en.getKey().substring(colon+1));
				int key = id << 16 | meta;
				Identifier ident = new Identifier(en.getValue().getAsString());
				data.put(key, Registry.ITEM.get(ident));
				data_id.put(key, ident);
			}
		} catch (Throwable t) {
			FabLog.warn("Failed to load legacy IDs", t);
		}
	}

	public static Item lookup(int id, int meta) {
		return data.get(id << 16 | meta);
	}

	public static Identifier lookup_id(int id, int meta) {
		return data_id.get(id << 16 | meta);
	}

}
