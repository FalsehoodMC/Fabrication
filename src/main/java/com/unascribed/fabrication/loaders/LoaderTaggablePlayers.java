package com.unascribed.fabrication.loaders;

import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.features.FeatureTaggablePlayers;
import com.unascribed.fabrication.support.ConfigLoader;

import java.nio.file.Path;

public class LoaderTaggablePlayers implements ConfigLoader {

	public static final LoaderTaggablePlayers instance = new LoaderTaggablePlayers();
//TODO check and filter features that are not viable like creepers_exlpode_when_on_fire
	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		for (String key : config.keySet()){
			FeatureTaggablePlayers.add(key, config.getInt(key).orElse(0), false);
		}
	}

	@Override
	public String getConfigName() {
		return "taggable_players";
	}

}
