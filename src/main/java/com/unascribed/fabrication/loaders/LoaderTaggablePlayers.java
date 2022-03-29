package com.unascribed.fabrication.loaders;

import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.features.FeatureTaggablePlayers;
import com.unascribed.fabrication.support.ConfigLoader;

import java.nio.file.Path;
import java.util.Optional;

public class LoaderTaggablePlayers implements ConfigLoader {

	public static final LoaderTaggablePlayers instance = new LoaderTaggablePlayers();

	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		for (String key : config.keySet()){
			Optional<Integer> i = config.getInt(key);
			if (i.isPresent()) {
				FeatureTaggablePlayers.add(key, i.get(), false);
				continue;
			}
			int val = 0;
			switch (config.get(key).orElse("").trim()) {
				case "untagged_players_only":
					val = 1;
					break;
				case "tagged_players":
					val = 2;
					break;
				case "untagged_players":
					val = 3;
					break;
			}
			FeatureTaggablePlayers.add(key, val, false);
		}
	}

	@Override
	public String getConfigName() {
		return "taggable_players";
	}

}
