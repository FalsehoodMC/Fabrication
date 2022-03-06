package com.unascribed.fabrication.loaders;

import com.google.common.collect.Sets;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.support.ConfigLoader;

import java.nio.file.Path;
import java.util.Set;

public class LoaderTaggablePlayers implements ConfigLoader {

	public static Set<String> activeTags = Sets.newHashSet();
	public static final LoaderTaggablePlayers instance = new LoaderTaggablePlayers();
	@Override
	public void remove(String key) {
		ConfigLoader.super.remove(FabConf.remap(key));
	}
	@Override
	public void set(String key, String val) {
		ConfigLoader.super.set(FabConf.remap(key), val);
	}
	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		for (String key : config.keySet()){
			activeTags.add("*"+key.substring(key.lastIndexOf('.')));
		}
	}

	@Override
	public String getConfigName() {
		return "taggable_players";
	}

}
