package com.unascribed.fabrication.loaders;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.features.FeatureTaggablePlayers;
import com.unascribed.fabrication.support.ConfigLoader;

import javax.swing.text.html.Option;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LoaderTaggablePlayers implements ConfigLoader {

	public static final LoaderTaggablePlayers instance = new LoaderTaggablePlayers();

	public void set(String key, int val) {
		switch (val){
			case 0:
				set(key, "tagged_players_only");
				break;
			case 1:
				set(key, "untagged_players_only");
				break;
			case 2:
				set(key, "tagged_players");
				break;
			case 3:
				set(key, "untagged_players");
				break;
			default:
				set(key, String.valueOf(val));
		}
	}

	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		for (String key : config.keySet()){
			if (key.length() > 1 && key.charAt(0) == '!') {
				if (key.startsWith("!player.")) {
					int i1 = key.indexOf('.', 8);
					if (i1 != -1) {
						String name = key.substring(8, i1);
						String featureKey = key.substring(i1 + 1);
						if (FabConf.isValid(featureKey)) {
							Map<String, Boolean> map = FeatureTaggablePlayers.playerNameOverrideMap.get(name);
							if (map == null)
								FeatureTaggablePlayers.playerNameOverrideMap.put(name, map = new HashMap<>());
							map.put(featureKey.substring(featureKey.lastIndexOf('.') + 1), parseOverride(config.get(key)));
							continue;
						}
					}
				} else if (key.startsWith("!player_uuid.")) {
					int i1 = key.indexOf('.', 13);
					if (i1 != -1) {
						String name = key.substring(13, i1);
						UUID uuid = null;
						try {
							uuid = UUID.fromString(name);
						} catch (IllegalArgumentException e) {
						}
						if (uuid != null) {
							String featureKey = key.substring(i1 + 1);
							if (FabConf.isValid(featureKey)) {
								Map<String, Boolean> map = FeatureTaggablePlayers.playerUUIDOverrideMap.get(uuid);
								if (map == null)
									FeatureTaggablePlayers.playerUUIDOverrideMap.put(uuid, map = new HashMap<>());
								map.put(featureKey.substring(featureKey.lastIndexOf('.') + 1), parseOverride(config.get(key)));
							}
							continue;
						}
					}
				}
				FabLog.error("TaggablePlayers config contains invalid key: "+key);
				continue;
			}
			if (!FabConf.isValid(key)) {
				FabLog.error("TaggablePlayers config contains invalid key: "+key);
				continue;
			}
			Optional<String> val = config.get(key);
			if (val.isPresent()) {
				try {
					FeatureTaggablePlayers.add(key, Integer.parseInt(val.get()), false);
					continue;
				} catch (IllegalArgumentException ignore){}
			}
			int i = 0;
			switch (val.orElse("").trim()) {
				case "untagged_players_only":
					i = 1;
					break;
				case "tagged_players":
					i = 2;
					break;
				case "untagged_players":
					i = 3;
					break;
			}
			FeatureTaggablePlayers.add(key, i, false);
		}
	}

	@Override
	public String getConfigName() {
		return "taggable_players";
	}
	private static boolean parseOverride(Optional<String> val) {
		if (!val.isPresent()) return false;
		switch (val.get().toLowerCase(Locale.ROOT)) {
			case "1" : case "true": return true;
		}
		return false;
	}
}
