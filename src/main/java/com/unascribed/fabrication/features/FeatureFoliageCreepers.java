package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@EligibleIf(configEnabled="*.foliage_creepers")
public class FeatureFoliageCreepers implements Feature {

	@Override
	public void apply() {
		if (Agnos.getCurrentEnv() == Env.CLIENT) {
			reloadClient();
		}
	}

	@Environment(EnvType.CLIENT)
	private void reloadClient() {
		if (MinecraftClient.getInstance().getResourceManager() != null) {
			MinecraftClient.getInstance().reloadResources();
		}
	}

	@Override
	public boolean undo() {
		if (Agnos.getCurrentEnv() == Env.CLIENT) {
			reloadClient();
		}
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.foliage_creepers";
	}

}