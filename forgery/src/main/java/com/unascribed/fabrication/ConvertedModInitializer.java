package com.unascribed.fabrication;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler;

public abstract class ConvertedModInitializer {

	public ConvertedModInitializer() {
		onInitialize();
		try {
			ModMenuAdapter mma = (ModMenuAdapter) Class.forName("com.unascribed.fabrication.ModMenuInitializer").getConstructor().newInstance();
			ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((mc, parent) -> {
				return mma.getModConfigScreenFactory().create(parent);
			}));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public abstract void onInitialize();

}
