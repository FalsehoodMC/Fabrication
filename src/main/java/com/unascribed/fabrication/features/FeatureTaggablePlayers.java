package com.unascribed.fabrication.features;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.loaders.LoaderTaggablePlayers;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@EligibleIf(configAvailable="*.taggable_players")
public class FeatureTaggablePlayers implements Feature {

	//TODO should probably be automatic
	public static final ImmutableSet<String> INVALID_TAGS = ImmutableSet.of(
			"weird_tweaks.extra.creepers_explode_when_on_fire"
	);

	public static final ImmutableMap<String, Integer> validTags;
	public static final ImmutableSet<String> listTags;
	public static Map<String, Integer> activeTags = new HashMap<>();

	static {
		Map<String, Integer> tags = new HashMap<>();
		Set<String> list = new HashSet<>();
		FeaturesFile.getAll().forEach((key, val)->{
			if (val.fscript == null || INVALID_TAGS.contains(key)) return;
			switch (val.fscript){
				case "PLAYER_ENTITY" :
				case "SERVER_PLAYER_ENTITY" :
					tags.put(key, 0b01);
					if (!val.extraFscript.isEmpty()) list.add(key);
					break;
				case "LIVING_ENTITY" :
				case "ENTITY" :
					tags.put(key, 0b11);
					if (!val.extraFscript.isEmpty()) list.add(key);
					break;
			}
		});
		validTags = ImmutableMap.copyOf(tags);
		listTags = ImmutableSet.copyOf(list);
	}

	public static Predicate<Object> getPredicate(String key, int type) {
		switch (type) {
			case 1:
				return pe -> pe instanceof TaggablePlayer && !((TaggablePlayer) pe).fabrication$hasTag(key);
			case 2:
				return pe -> !(pe instanceof TaggablePlayer) || ((TaggablePlayer) pe).fabrication$hasTag(key);
			case 3:
				return pe -> !(pe instanceof TaggablePlayer) || !((TaggablePlayer) pe).fabrication$hasTag(key);
			default:
				return pe -> pe instanceof TaggablePlayer && ((TaggablePlayer) pe).fabrication$hasTag(key);
		}
	}

	private static void set(String key, int type){
		Predicate p = getPredicate(key.substring(key.lastIndexOf('.')+1), type);
		if (listTags.contains(key)){
			Predicate tmp = p;
			p = o -> tmp.test(((List<Object>)o).get(0));
		}
		Predicate<?> defPredicate = ConfigPredicates.defaults.get(key);
		ConfigPredicates.put(key, defPredicate != null ? p.and(defPredicate) : p, 1);
	}

	public static void add(String key, int type) {
		add(key, type, true);
	}

	public static void add(String key, int type, boolean save) {
		type &= validTags.get(key);
		if (FabConf.isEnabled("*.taggable_players")) {
			set(key, type);
		}
		activeTags.put(key, type);
		if (save) LoaderTaggablePlayers.instance.set(key, type);
	}
	public static void remove(String key) {
		ConfigPredicates.remove(key, 1);
		activeTags.remove(key);
		LoaderTaggablePlayers.instance.remove(key);
	}

	@Override
	public void apply() {
		activeTags.forEach(FeatureTaggablePlayers::set);
	}

	@Override
	public boolean undo() {
		activeTags.keySet().forEach(k->ConfigPredicates.remove(k, 1));
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.taggable_players";
	}



}
