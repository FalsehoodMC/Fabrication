package com.unascribed.fabrication;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import javax.net.ssl.HttpsURLConnection;

import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.jna.Platform;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.ResolvedTrilean;
import com.unascribed.fabrication.support.Trilean;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import com.google.common.net.UrlEscapers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public class Analytics {

	private static final Executor exec = Executors.newSingleThreadExecutor(r -> new Thread(r, "Fabrication analytics submission thread") {{setDaemon(true);}});
	
	private static String userId = null;
	private static long last = -1;
	private static long first = -1;
	
	public static void deleteId() {
		userId = null;
		last = -1;
		first = -1;
		timerFile().delete();
		userIdFile().delete();
		FabLog.info("Analytics user ID deleted due to opt-out");
	}
	
	public static void submit(String action) {
		submit(action, Collections.emptyMap());
	}
	
	public static void submit(String action, Map<String, String> extraScoped) {
		if (!MixinConfigPlugin.isEnabled("*.data_upload")) return;
		exec.execute(() -> {
			ensureUserIdPresent();
			Escaper ue = UrlEscapers.urlPathSegmentEscaper();
			try {
				byte[] rand = new byte[8];
				ThreadLocalRandom.current().nextBytes(rand);
				Map<String, String> params = Maps.newLinkedHashMap();
				params.put("_id", userId);
				params.put("_idts", Long.toString(first));
				params.put("_viewts", Long.toString(last));
				params.put("url", "https://unascribed.com/fabrication");
				params.put("idsite", "2");
				params.put("rec", "1");
				params.put("apiv", "1");
				params.put("action_name", action);
				params.put("rand", BaseEncoding.base16().encode(rand));
				params.put("cookie", "0");
				params.put("send_image", "0");
				Map<String, String> extra = Maps.newLinkedHashMap();
				extra.put("Version", Agnos.getModVersion());
				extra.put("OS", Platform.isWindows() ? "Windows" : Platform.isMac() ? "macOS" : Platform.isLinux() ? "Linux" : "Unknown (BSD?)");
				extra.put("Java Version", System.getProperty("java.version"));
				extra.put("Java Vendor", System.getProperty("java.vendor"));
				String vm = System.getProperty("java.vm.name");
				if (vm.endsWith("Zero VM")) {
					vm = "Zero";
				} else if (vm.endsWith("Server VM") || vm.endsWith("Client VM")) {
					vm = "HotSpot";
				} else if (vm.contains("OpenJ9")) {
					vm = "OpenJ9";
				} else {
					vm = "Unknown ("+vm+")";
				}
				extra.put("Java VM", vm);
				extra.put("Minecraft Version", SharedConstants.getGameVersion().getName());
				extra.put("Loader", (FabRefl.FORGE ? "Forge" : "Fabric")+" "+Agnos.getLoaderVersion());
				extra.put("Environment", Agnos.getCurrentEnv().name());
				extra.put("Profile", MixinConfigPlugin.getRawValue("general.profile"));
				if (Agnos.getCurrentEnv() == Env.CLIENT) {
					addClientData(extra);
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
			touch();
		});
	}
	
	public static void submitConfig() {
		if (!MixinConfigPlugin.isEnabled("*.data_upload")) return;
		exec.execute(() -> {
			ensureUserIdPresent();
			Escaper ue = UrlEscapers.urlPathSegmentEscaper();
			try {
				JsonArray requests = new JsonArray();
				for (String key : MixinConfigPlugin.getAllKeys()) {
					ResolvedTrilean rt = MixinConfigPlugin.getResolvedValue(key);
					requests.add("?_id="+userId+"&_idts="+first+"&_viewts="+last+
							"&url=https://unascribed.com/fabrication/value/"+rt.value+
							"&idsite=2&rec=1&apiv=1&action_name="+ue.escape(key)+"&cookie=0"+
							"&cvar="+ue.escape("{\"1\":[\"By Profile\",\""+(rt.trilean == Trilean.UNSET ? "Yes" : "No")+"\"]}"));
				}
				JsonObject body = new JsonObject();
				body.add("requests", requests);
				URL u = new URL("https://maxim.sleeping.town/matomo.php");
				HttpsURLConnection conn = (HttpsURLConnection)u.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
				FabLog.debug("Submitting feature analytics: "+u);
				conn.connect();
				try (OutputStream os = conn.getOutputStream()) {
					os.write(new Gson().toJson(body).getBytes(Charsets.UTF_8));
				}
				conn.getInputStream().close();
			} catch (Throwable e) {
				FabLog.warn("Failed to submit Matomo data", e);
			}
			touch();
		});
	}
	
	private static File userHome() {
		return new File(System.getProperty("user.home"));
	}
	
	private static File timerFile() {
		return new File(userHome(), ".fabrication-timer");
	}
	
	private static File userIdFile() {
		return new File(userHome(), ".fabrication-user-id");
	}
	
	private static void touch() {
		File timerFile = timerFile();
		last = System.currentTimeMillis()/1000L;
		timerFile.setLastModified(System.currentTimeMillis());
	}
	
	private static void ensureUserIdPresent() {
		File timerFile = timerFile();
		if (userId == null) {
			File userIdFile = userIdFile();
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
				FabLog.info("Analytics user ID created: "+userId);
				try {
					Files.write(userId, userIdFile, Charsets.UTF_8);
				} catch (IOException e) {
					FabLog.warn("Failed to save Matomo user id", e);
				}
			}
			first = userIdFile.lastModified()/1000L;
			last = timerFile.lastModified()/1000L;
		}
	}

	@Environment(EnvType.CLIENT)
	private static void addClientData(Map<String, String> extra) {
		try {
			MinecraftClient.getInstance().submitAndJoin(() -> {
				extra.put("OpenGL Version", GL11.glGetString(GL11.GL_VERSION));
				Window w = MinecraftClient.getInstance().getWindow();
				double ratio = w.getWidth()/(double)w.getHeight();
				String ratioStr;
				if (isBasically(ratio, 1)) {
					ratioStr = "1:1";
				} else if (isBasically(ratio, 16/9D)) {
					ratioStr = "16:9";
				} else if (isBasically(ratio, 4/3D)) {
					ratioStr = "4:3";
				} else if (isBasically(ratio, 5/4D)) {
					ratioStr = "5:4";
				} else if (isBasically(ratio, 21/9D)) {
					ratioStr = "21:9";
				} else if (isBasically(ratio, 2/1D)) {
					ratioStr = "2:1";
				} else if (isBasically(ratio, 9/16D)) {
					ratioStr = "9:16";
				} else if (isBasically(ratio, 3/4D)) {
					ratioStr = "3:4";
				} else if (isBasically(ratio, 4/5D)) {
					ratioStr = "4:5";
				} else if (isBasically(ratio, 9/21D)) {
					ratioStr = "9:21";
				} else if (isBasically(ratio, 1/2D)) {
					ratioStr = "1:2";
				} else if (ratio < 4/3D) {
					ratioStr = "? (narrower than 4:3)";
				} else if (ratio < 16/9D) {
					ratioStr = "? (narrower than 16:9)";
				} else if (ratio > 21/9D) {
					ratioStr = "? (wider than 21:9)";
				} else {
					ratioStr = "? (wider than 16:9)";
				}
				extra.put("Aspect Ratio", ratioStr);
				extra.put("Language", MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode());
			});
		} catch (Throwable e) {
			extra.put("OpenGL Version", "<error>");
			extra.put("Aspect Ratio", "<error>");
			extra.put("Language", "<error>");
		}
	}

	private static boolean isBasically(double a, double b) {
		return Math.abs(a-b) < 0.05;
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
