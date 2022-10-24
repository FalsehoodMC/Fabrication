package com.unascribed.fabrication.loaders;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.google.common.base.Charsets;
import com.google.common.io.MoreFiles;

@EligibleIf(envMatches=Env.CLIENT)
public class LoaderFScript implements ConfigLoader {

	private static Map<String, String> scripts = new HashMap<>();
	private static Path lastPath;
	public static String get(String key) {
		if (scripts.containsKey(key)) return scripts.get(key);
		return FeaturesFile.get(key).fscriptDefault;
	}

	public static Map<String, String> getMap() {
		return scripts;
	}
	public static void put(String key, String script) {
		if(script == null) scripts.remove(key);
		else scripts.put(key, script);

		save();
	}
	public static void save() {
		StringBuilder builder = new StringBuilder();
		AtomicReference<String> category = new AtomicReference<>();
		scripts.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((entry) -> {
			String key1 = entry.getKey();
			int i = 0;
			if(key1.contains(".")){
				i = key1.lastIndexOf('.');
				String cat = key1.substring(0, i);
				if (!cat.equals(category.get())){
					builder.append('[').append(key1, 0, i).append(']').append('\n');
					category.set(cat);
				}
				i++;
			}
			builder.append(key1, i, key1.length()).append('=').append(entry.getValue()).append('\n');
		});

		try {
			if (lastPath != null) MoreFiles.asCharSink(lastPath, Charsets.UTF_8).write(builder);
		}catch (Exception e){
			FabLog.error("Failed to write fscript.ini", e);
		}
	}
	public static void reload(){
		if(lastPath == null) return;
		scripts.clear();
		try {
			QDIni.loadAndTransform(lastPath.toString(), new InputStreamReader(Files.newInputStream(lastPath), StandardCharsets.UTF_8), new QDIni.IniTransformer() {
				@Override
				public String transformLine(String path, String line) {
					return line;
				}

				@Override
				public String transformValueComment(String key, String value, String comment) {
					return comment;
				}

				@Override
				public String transformValue(String key, String value) {
					scripts.put(key, value);
					return value;
				}
			}, null, false);
		}catch (Exception e){
			FabLog.error("Failed to load fscript.ini", e);
		}
	}
	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		lastPath = configDir.resolve(getConfigName()+".ini");
		reload();
	}

	@Override
	public String getConfigName() {
		return "fscript";
	}

}
