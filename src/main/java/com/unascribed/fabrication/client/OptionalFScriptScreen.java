package com.unascribed.fabrication.client;

import io.github.queerbric.pride.PrideFlag;
import net.minecraft.client.gui.screen.Screen;

public class OptionalFScriptScreen {

	public static Screen construct(Screen parent, PrideFlag prideFlag, String title, String configKey) {
		return new FScriptScreen(parent, prideFlag, title, configKey);
	}

}