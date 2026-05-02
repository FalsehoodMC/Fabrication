package com.unascribed.fabrication.client;

import io.github.queerbric.pride.PrideClient;
import io.github.queerbric.pride.PrideFlag;
import io.github.queerbric.pride.PrideFlags;

public class PrideFlagProvider {
	public static PrideFlagRenderer get() {
		if (PrideFlags.isPrideMonth()){
			PrideFlag ret = PrideFlags.getRandomFlag();
			if (ret != null) return ret::render;
		}
		return null;
	}

	public static void init() {
		new PrideClient().onInitializeClient();
	}
}
