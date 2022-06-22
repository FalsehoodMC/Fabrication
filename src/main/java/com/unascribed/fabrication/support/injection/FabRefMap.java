package com.unascribed.fabrication.support.injection;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.FabConst;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FabRefMap {
	public static String relativeMap(String mixinClass, String method){
		return map(methodMap.get(mixinClass), method);
	}
	public static String absoluteMap(String method){
		return map(targetMap, method);
	}
	public static String map(Map<String, String> map, String method) {
		if (map == null) return method;
		String ret = map.get(method);
		return ret == null ? method : ret;
	}
	public static final Map<String, Map<String, String>> methodMap = new HashMap<>();
	public static final Map<String, String> targetMap = new HashMap<>();
	static {
		try {
			InputStream is = FabRefMap.class.getResourceAsStream("/fabAbsRefMap.txt");
			BufferedReader read = new BufferedReader(new InputStreamReader(is));
			int line = -1;
			while (true){
				String l = read.readLine();
				if (l == null) break;
				line++;
				int i = l.indexOf(' ');
				if (i == -1) {
					FabLog.error("Bad relative key on line "+line);
					continue;
				}
				targetMap.put(l.substring(0, i), l.substring(i + 1));
			}
		} catch (Exception e) {
			if (!FabConst.DEV) {
				FabLog.error("Failed to parse fabAbsRefMap", e);
			}
		}
		try {
			InputStream is = FabRefMap.class.getResourceAsStream("/fabRelRefMap.txt");
			BufferedReader read = new BufferedReader(new InputStreamReader(is));
			int line = 0;
			while (true) {
				String key = read.readLine();
				line++;
				if (key == null) break;
				if (key.isBlank()) {
					FabLog.warn("FabRefMap bad value on line "+line);
					continue;
				}
				if (!methodMap.containsKey(key)) methodMap.put(key, new HashMap<>());

				String l = read.readLine();
				line++;
				if (l == null) break;
				for (String m : l.split("\t")) {
					int i = m.indexOf(' ');
					if (i == -1) {
						continue;
					}
					methodMap.get(key).put(m.substring(0, i), m.substring(i+1));
				}
			}
		} catch (Exception e) {
			if (!FabConst.DEV) {
				FabLog.error("Failed to parse fabRelRefMap", e);
			}
		}
	}
}
