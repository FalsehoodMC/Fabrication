package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

@EligibleIf(configAvailable="*.old_lava_scaling", envMatches=Env.CLIENT)
public class FeatureOldLavaScaling implements Feature {

	@Override
	public void apply(MinecraftServer minecraftServer, World world) {
		if (MinecraftClient.getInstance().getResourceManager() != null) {
			MinecraftClient.getInstance().reloadResources();
		}
	}

	@Override
	public boolean undo(MinecraftServer minecraftServer, World world) {
		apply(minecraftServer, world);
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.old_lava_scaling";
	}

}
