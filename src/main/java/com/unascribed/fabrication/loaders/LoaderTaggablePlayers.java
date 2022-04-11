package com.unascribed.fabrication.loaders;

import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.features.FeatureTaggablePlayers;
import com.unascribed.fabrication.support.ConfigLoader;

import java.nio.file.Path;
import java.util.Optional;

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

}
