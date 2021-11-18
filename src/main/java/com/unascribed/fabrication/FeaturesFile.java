package com.unascribed.fabrication;

import java.io.Reader;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

public final class FeaturesFile {

	public enum Sides {
		IRRELEVANT,
		EITHER,
		CLIENT_ONLY,
		SERVER_ONLY,
		SERVER_ONLY_WITH_CLIENT_HELPER,
		SERVER_AND_CLIENT,
	}

	public static final class FeatureEntry {
		public final String key;
		public final String name;
		public final String shortName;
		public final boolean meta;
		public final boolean section;
		public final boolean extra;
		public final String since;
		public final int sinceCode;
		public final Sides sides;
		public final ImmutableSet<String> needs;
		public final String def;
		public final String parent;
		public final String media;
		public final String mediaText;
		public final String extraMedia;
		public final String extraMediaText;
		public final String linkUrl;
		public final String linkText;
		public final String desc;
		public final String fscript;
		public final String fscriptDefault;
		public final String extend;

		public FeatureEntry(String key, JsonObject obj) {
			this.key = key;
			name = get(obj, "name", JsonElement::getAsString, null);
			shortName = get(obj, "short_name", JsonElement::getAsString, null);
			meta = get(obj, "meta", JsonElement::getAsBoolean, false);
			section = get(obj, "section", JsonElement::getAsBoolean, false);
			extra = get(obj, "extra", JsonElement::getAsBoolean, false);
			since = get(obj, "since", JsonElement::getAsString, null);
			sinceCode = get(obj, "since_code", JsonElement::getAsInt, -1);
			sides = Sides.valueOf(get(obj, "sides", JsonElement::getAsString, "irrelevant").toUpperCase(Locale.ROOT));
			needs = ImmutableSet.copyOf(get(obj, "needs", FeatureEntry::getAsStringSet, Collections.emptySet()));
			def = get(obj, "default", JsonElement::getAsString, "inherit");
			parent = get(obj, "parent", JsonElement::getAsString, null);
			media = get(obj, "media", JsonElement::getAsString, null);
			mediaText = get(obj, "media_text", JsonElement::getAsString, null);
			extraMedia = get(obj, "extra_media", JsonElement::getAsString, null);
			extraMediaText = get(obj, "extra_media_text", JsonElement::getAsString, null);
			linkUrl = get(obj, "link_url", JsonElement::getAsString, null);
			linkText = get(obj, "link_text", JsonElement::getAsString, null);
			desc = get(obj, "desc", JsonElement::getAsString, "No description");
			fscript = get(obj, "fscript", s -> s.getAsString().toUpperCase(Locale.ROOT), null);
			fscriptDefault = get(obj, "fscript_default", JsonElement::getAsString, null);
			extend = get(obj, "extend", JsonElement::getAsString, null);
		}

		private static <T> T get(JsonObject obj, String key, Function<JsonElement, T> func, T def) {
			JsonElement ele = obj.get(key);
			if (ele == null || ele.isJsonNull()) return def;
			return func.apply(ele);
		}
		private static Set<String> getAsStringSet(JsonElement ele) {
			return Sets.newHashSet(Iterables.transform(ele.getAsJsonArray(), JsonElement::getAsString));
		}

		@Override
		public String toString() {
			return "FeatureEntry [key=" + key + ", name=" + name
					+ ", shortName=" + shortName + ", meta=" + meta
					+ ", section=" + section + ", extra=" + extra + ", since="
					+ since + ", sinceCode=" + sinceCode + ", sides=" + sides
					+ ", needs=" + needs + ", def=" + def + ", parent=" + parent
					+ ", media=" + media + ", mediaText=" + mediaText
					+ ", extraMedia=" + extraMedia + ", extraMediaText="
					+ extraMediaText + ", linkUrl=" + linkUrl + ", linkText="
					+ linkText + ", desc=" + desc + ", fscript=" + fscript
					+ ", fscriptDefault=" + fscriptDefault + ", extend="
					+ extend + "]";
		}

	}

	private static final ImmutableMap<String, FeatureEntry> data;
	static {
		ImmutableMap.Builder<String, FeatureEntry> bldr = ImmutableMap.builder();
		try (Reader r = Resources.asCharSource(FeaturesFile.class.getClassLoader().getResource("features.json"), Charsets.UTF_8).openStream()) {
			JsonObject obj = new Gson().fromJson(r, JsonObject.class);
			for (Map.Entry<String, JsonElement> en : obj.entrySet()) {
				bldr.put(en.getKey(), new FeatureEntry(en.getKey(), en.getValue().getAsJsonObject()));
			}
		} catch (Throwable t) {
			FabLog.warn("Failed to load features.json", t);
		}
		data = bldr.build();
	}

	private static final FeatureEntry defaultEntry = new FeatureEntry("", new JsonObject());

	public static FeatureEntry get(String key) {
		return data.getOrDefault(key, defaultEntry);
	}

	public static ImmutableMap<String, FeatureEntry> getAll() {
		return data;
	}


}
