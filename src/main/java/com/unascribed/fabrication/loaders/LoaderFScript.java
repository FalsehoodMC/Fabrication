package com.unascribed.fabrication.loaders;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@EligibleIf(envMatches=Env.CLIENT)
public class LoaderFScript implements ConfigLoader {

	private static Map<String, String> scripts = new HashMap<>();
	private static Path lastPath;
	public static String get(String key) {
		if (scripts.containsKey(key)) return scripts.get(key);
		return FeaturesFile.get(key).fscriptDefault;
	}
	//TODO save to file
	public static void put(String key, String script) {
		if(script == null) scripts.remove(key);
		else scripts.put(key, script);
	}

	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		lastPath = configDir.resolve(getConfigName()+".ini");
		scripts.clear();
		try {
			config = QDIni.loadAndTransform(lastPath.toString(), new InputStreamReader(Files.newInputStream(lastPath), StandardCharsets.UTF_8), null, null, false);
			for (String k : config.keySet()) {
				config.get(k).ifPresent(s -> scripts.put(k, s));
			}
		}catch (Exception e){
			FabLog.error("Failed to load fscript.ini", e);
		}
	}

	@Override
	public String getConfigName() {
		return "fscript";
	}

}
