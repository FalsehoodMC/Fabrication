package com.unascribed.fabrication.client;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FeaturesFile;
import io.github.queerbric.pride.PrideFlag;
import net.minecraft.client.gui.screen.Screen;

public class OptionalFScriptScreen {

	public static Screen construct(Screen parent, PrideFlag prideFlag, String title, String configKey) {
		FeaturesFile.FeatureEntry feature = FeaturesFile.get(configKey);
		if (feature.fscript == null) {
			title = FeaturesFile.get(FabConf.remap(feature.extend)).name;
			configKey = FabConf.remap(feature.extend);
		}
		return new FScriptScreen(parent, prideFlag, title, configKey);
	}

}
