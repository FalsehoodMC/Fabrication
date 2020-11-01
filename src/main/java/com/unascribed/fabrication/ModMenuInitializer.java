package com.unascribed.fabrication;

import com.unascribed.fabrication.client.FabricationConfigScreen;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

public class ModMenuInitializer implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return FabricationConfigScreen::new;
	}
	
}
