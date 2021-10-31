package com.unascribed.fabrication.loaders;

import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@EligibleIf(envMatches=Env.CLIENT)
public class LoaderFScript implements ConfigLoader {

	private static Map<String, String> scripts = new HashMap<>();
	
	public static String get(String key) {
		if (scripts.containsKey(key)) return scripts.get(key);
		return FeaturesFile.get(key).fscriptDefault;
	}
	//TODO save to file
	public static void put(String key, String script) {
		if(script == null) scripts.remove(key);
		else scripts.put(key, script);
	}
	//TODO check if any of the special chars mess with .ini
	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		scripts.clear();
		for (String k : config.keySet()) {
			config.get(k).ifPresent(s -> scripts.put(k, s));
		}
	}

	@Override
	public String getConfigName() {
		return "fscript";
	}

}
