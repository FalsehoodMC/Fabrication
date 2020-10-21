package com.unascribed.fabrication;

import java.util.Map;

import com.unascribed.fabrication.support.MixinConfigPlugin;

import com.google.common.collect.Maps;

import net.fabricmc.api.ModInitializer;

public class FabricationMod implements ModInitializer {
	
	private final Map<String, Runnable> features = Maps.newHashMap();
	
	@Override
	public void onInitialize() {
		for (String s : MixinConfigPlugin.discoverClassesInPackage("com.unascribed.fabrication.features", false)) {
			try {
				Runnable r = (Runnable)Class.forName(s).newInstance();
				r.run();
				features.put(s, r);
			} catch (Exception e) {
				throw new RuntimeException("Failed to initialize feature "+s, e);
			}
			
		}
	}
}
