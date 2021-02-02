package com.unascribed.fabrication;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.jna.Platform;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;

public class Analytics {

	private static final Executor exec = Executors.newSingleThreadExecutor();
	
	private static String userId = null;
	private static long last = -1;
	private static long first = -1;
	
	public static void submit(String action, String... extraKv) {
		if (!MixinConfigPlugin.isEnabled("*.data_upload")) return;
		exec.execute(() -> {
			boolean didGenerate = false;
			File timerFile = new File(new File(System.getProperty("user.home")), ".fabrication-timer");
			if (userId == null) {
				File userIdFile = new File(new File(System.getProperty("user.home")), ".fabrication-user-id");
				if (!timerFile.exists()) {
					try {
						timerFile.createNewFile();
					} catch (IOException e) {
						FabLog.warn("Failed to create timer file", e);
					}
				}
				boolean needGenerate = true;
				if (userIdFile.exists()) {
					try {
						String uid = Files.toString(userIdFile, Charsets.UTF_8).trim();
						if (uid.length() == 16 && CharMatcher.anyOf("0123456789abcdefABCDEF").matchesAllOf(uid)) {
							needGenerate = false;
							userId = uid.trim();
						}
					} catch (IOException e) {
						FabLog.warn("Failed to load Matomo user id", e);
					}
				}
				if (needGenerate) {
					userId = BaseEncoding.base16().encode(SecureRandom.getSeed(8));
					didGenerate = true;
					try {
						Files.write(userId, userIdFile, Charsets.UTF_8);
					} catch (IOException e) {
						FabLog.warn("Failed to save Matomo user id", e);
					}
				}
				first = userIdFile.lastModified()/1000L;
				last = timerFile.lastModified()/1000L;
			}
			Escaper ue = UrlEscapers.urlPathSegmentEscaper();
			try {
				byte[] rand = new byte[8];
				ThreadLocalRandom.current().nextBytes(rand);
				Map<String, String> params = Maps.newLinkedHashMap();
				params.put("_id", userId);
				params.put("_idts", Long.toString(first));
				params.put("_viewts", Long.toString(last));
				params.put("url", "fabrication://"+(FabRefl.FORGE ? "forge" : "fabric")+"/"+Agnos.getModVersion()+"/"+action);
				params.put("new_visit", didGenerate ? "1" : "0");
				params.put("idsite", "1");
				params.put("rec", "1");
				params.put("apiv", "1");
				params.put("action_name", action);
				params.put("rand", BaseEncoding.base16().encode(rand));
				params.put("ua", "Java/"+System.getProperty("java.version"));
				params.put("cookie", "0");
				params.put("send_image", "0");
				Map<String, String> extra = Maps.newLinkedHashMap();
				extra.put("OS", Platform.isWindows() ? "Windows" : Platform.isMac() ? "macOS" : Platform.isLinux() ? "Linux" : "Unknown (BSD?)");
				extra.put("Java version", System.getProperty("java.version"));
				extra.put("Java vendor", System.getProperty("java.vendor"));
				extra.put("Java VM", System.getProperty("java.vm.name"));
				extra.put("Loader version", Agnos.getLoaderVersion());
				extra.put("Environment", Agnos.getCurrentEnv().name());
				Map<String, String> extraScoped = Maps.newLinkedHashMap();
				for (int i = 0; i < extraKv.length; i += 2) {
					extraScoped.put(extraKv[i], extraKv[i+1]);
				}
				params.put("_cvar", processExtra(extra));
				params.put("cvar", processExtra(extraScoped));
				String paramStr = Joiner.on("&").withKeyValueSeparator("=").join(Maps.transformValues(params, ue::escape));
				URL u = new URL("https://maxim.sleeping.town/matomo.php?"+paramStr);
				FabLog.debug("Submitting analytics: "+u);
				u.openStream().close();
			} catch (Throwable e) {
				FabLog.warn("Failed to submit Matomo data", e);
			}
			last = System.currentTimeMillis()/1000L;
			timerFile.setLastModified(System.currentTimeMillis());
		});
	}

	private static String processExtra(Map<String, String> extra) {
		JsonObject extraJson = new JsonObject();
		int i = 1;
		for (Map.Entry<String, String> en : extra.entrySet()) {
			JsonArray arr = new JsonArray();
			arr.add(en.getKey());
			arr.add(en.getValue());
			extraJson.add(Integer.toString(i), arr);
			i++;
		}
		return new Gson().toJson(extraJson);
	}
	
}
