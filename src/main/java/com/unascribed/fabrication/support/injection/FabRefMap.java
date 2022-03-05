package com.unascribed.fabrication.support.injection;

import com.unascribed.fabrication.FabLog;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FabRefMap {
	public static String methodMap(String mixinClass, String method){
		Map<String, String> map = methodMap.get(mixinClass);
		if (map == null) return method;
		String ret = map.get(method);
		return ret == null ? method : ret;
	}
	public static String targetMap(String mixinClass, String method){
		Map<String, String> map = targetMap.get(mixinClass);
		if (map == null) return method;
		String ret = map.get(method);
		return ret == null ? method : ret;
	}
	public static final Map<String, Map<String, String>> methodMap = new HashMap<>();
	public static final Map<String, Map<String, String>> targetMap = new HashMap<>();
	static {
		boolean printErr = true;
		try {
			Class.forName("net.minecraft.util.Identifier", false, FabRefMap.class.getClassLoader());
			printErr = false;
		} catch (ClassNotFoundException ignore) {}
		try {
			InputStream is = FabRefMap.class.getResourceAsStream("/fabRefMap.txt");
			BufferedReader read = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String key = read.readLine();
				if (key == null) break;
				if (!methodMap.containsKey(key)) methodMap.put(key, new HashMap<>());
				if (!targetMap.containsKey(key)) targetMap.put(key, new HashMap<>());

				String l = read.readLine();
				if (l == null) break;
				for (String m : l.split("\t")) {
					int i = m.indexOf(' ');
					methodMap.get(key).put(m.substring(0, i), m.substring(i+1));
				}
				l = read.readLine();
				if (l == null) break;
				for (String m : l.split("\t")) {
					int i = m.indexOf(' ');
					targetMap.get(key).put(m.substring(0, i), m.substring(i+1));
				}
			}
		} catch (Exception e) {
			if (printErr) {
				FabLog.error("Could not parse fabRefMap", e);
			}
		}
	}
}
