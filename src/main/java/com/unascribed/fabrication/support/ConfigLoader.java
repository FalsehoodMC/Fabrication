package com.unascribed.fabrication.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.QDIni;

public interface ConfigLoader {

	void load(Path configDir, QDIni config, boolean loadError);

	String getConfigName();

	default void remove(String key) {
		Stopwatch watch = Stopwatch.createStarted();
		StringWriter sw = new StringWriter();
		Path configFile = Agnos.getConfigDir().resolve("fabrication").resolve(getConfigName()+".ini");
		try {
			QDIni.loadAndTransform(configFile, QDIni.simpleLineIniTransformer(((path, line) -> {
				if (line == null) return null;
				int i = line.indexOf('=');
				if (i != -1 && key.equals(path+line.substring(0, i))) {
					return null;
				}
				return line;
			})), sw);
			Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
			FabLog.info("Update of "+configFile+" done in "+watch);
		} catch (IOException e) {
			FabLog.warn("Failed to update "+configFile+" file", e);
		}
	}

	default void set(String key, String val) {
		Stopwatch watch = Stopwatch.createStarted();
		StringWriter sw = new StringWriter();
		Path configFile = Agnos.getConfigDir().resolve("fabrication").resolve(getConfigName()+".ini");
		try {
			AtomicBoolean found = new AtomicBoolean(false);
			QDIni.loadAndTransform(configFile, QDIni.simpleValueIniTransformer(((key1, value) -> {
				if (key1.equals(key)){
					found.set(true);
					return val;
				}
				return value;
			})), sw);
			if (!found.get()){
				StringWriter sw2 = new StringWriter();
				QDIni.loadAndTransform(getConfigName() + " internal append value", new ByteArrayInputStream(sw.toString().getBytes()),
						new QDIni.IniTransformer() {
							boolean insertNext = false;

							@Override
							public String transformLine(String path, String line) {
								if (!found.get()) {
									if (insertNext) {
										found.set(true);
										if (line == null) return key + "=" + val;
										else return key + "=" + val + "\n" + line;
									}
									if (line != null) {
										if (line.startsWith("[]")) {
											insertNext = true;
										} else if (path != null && !path.isEmpty() && key.startsWith(path)) {
											found.set(true);
											return key.substring(path.length()+1) + "=" + val + "\n" + line;
										}
									} else {
										found.set(true);
										int dot = key.lastIndexOf('.');
										if (dot == -1) {
											return "[]\n" + key + "=" + val;
										} else {
											return "[" + key.substring(0, dot) + "]\n" + key.substring(dot+1) + "=" + val;
										}
									}
								}
								return line;
							}

							@Override
							public String transformValueComment(String key, String value, String comment) {
								return comment;
							}

							@Override
							public String transformValue(String key, String value) {
								return value;
							}
						}, sw2);
				Files.write(configFile, sw2.toString().getBytes(Charsets.UTF_8));
			} else {
				Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
			}
			FabLog.info("Update of "+configFile+" done in "+watch);
		} catch (IOException e) {
			FabLog.warn("Failed to update "+configFile+" file", e);
		}
	}


}
