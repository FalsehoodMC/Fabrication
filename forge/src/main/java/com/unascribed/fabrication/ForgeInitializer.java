package com.unascribed.fabrication;

import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod("fabrication")
public class ForgeInitializer{
	public ForgeInitializer() {
		new FabricationMod().onInitialize();
		try {
			ModMenuAdapter mma = (ModMenuAdapter) Class.forName("com.unascribed.fabrication.ModMenuInitializer").getConstructor().newInstance();
			ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((mc, parent) -> {
				return mma.getModConfigScreenFactory().create(parent);
			}));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
