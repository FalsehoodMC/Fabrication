package com.unascribed.fabrication.support.injection;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.FabConst;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FabRefMap {
	public static String methodMap(String mixinClass, String method){
		return map(methodMap.get(mixinClass), method);
	}
	public static String targetMap(String mixinClass, String method){
		return map(targetMap.get(mixinClass), method);
	}
	public static String map(Map<String, String> map, String method) {
		if (map == null) return method;
		String ret = map.get(method);
		return ret == null ? method : ret;
	}
	public static final Map<String, Map<String, String>> methodMap = new HashMap<>();
	public static final Map<String, Map<String, String>> targetMap = new HashMap<>();
	static {
		try {
			InputStream is = FabRefMap.class.getResourceAsStream("/fabRefMap.txt");
			BufferedReader read = new BufferedReader(new InputStreamReader(is));
			int line = 0;
			while (true) {
				String key = read.readLine();
				line++;
				if (key == null) break;
				if (key.isBlank()) {
					FabLog.error("Bad key on line "+line);
					continue;
				}
				if (!methodMap.containsKey(key)) methodMap.put(key, new HashMap<>());
				if (!targetMap.containsKey(key)) targetMap.put(key, new HashMap<>());

				String l = read.readLine();
				line++;
				if (l == null) break;
				for (String m : l.split("\t")) {
					int i = m.indexOf(' ');
					if (i == -1) {
						FabLog.error("Bad method mapping for "+key+" on line "+line);
						continue;
					}
					methodMap.get(key).put(m.substring(0, i), m.substring(i+1));
				}
				l = read.readLine();
				line++;
				if (l == null) break;
				for (String m : l.split("\t")) {
					int i = m.indexOf(' ');
					if (i == -1) {
						FabLog.error("Bad target mapping for "+key+" on line "+line);
						continue;
					}
					targetMap.get(key).put(m.substring(0, i), m.substring(i+1));
				}
			}
		} catch (Exception e) {
			if (!FabConst.DEV) {
				FabLog.error("Could not parse fabRefMap", e);
			}
		}
	}
}
