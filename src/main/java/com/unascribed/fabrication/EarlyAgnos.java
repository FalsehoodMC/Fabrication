package com.unascribed.fabrication;

import com.unascribed.fabrication.support.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class EarlyAgnos {
	public static Env getCurrentEnv() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	public static boolean isModLoaded(String modid) {
		if (modid.startsWith("forge:")) return false;
		if (modid.startsWith("fabric:")) modid = modid.substring(7);
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	public static String getModVersion() {
		return FabricLoader.getInstance().getModContainer("fabrication").get().getMetadata().getVersion().getFriendlyString();
	}

	public static boolean isForge() {
		return false;
	}

	public static String getLoaderVersion() {
		return FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString();
	}

	public static Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}
}
