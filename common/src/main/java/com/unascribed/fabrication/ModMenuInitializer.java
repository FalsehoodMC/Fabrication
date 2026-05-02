package com.unascribed.fabrication;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.unascribed.fabrication.client.FabricationConfigScreen;

public class ModMenuInitializer implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return FabricationConfigScreen::new;
	}

}
