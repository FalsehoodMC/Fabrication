package com.unascribed.fabrication;

import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.fml.ModLoadingContext;

public abstract class ConvertedModInitializer {

	public ConvertedModInitializer() {
		onInitialize();
		try {
			ModMenuAdapter mma = (ModMenuAdapter) Class.forName("com.unascribed.fabrication.ModMenuInitializer").getConstructor().newInstance();
			ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((mc, parent) -> {
				return mma.getModConfigScreenFactory().create(parent);
			}));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public abstract void onInitialize();
	
}
