package com.unascribed.fabrication.client;

import io.github.queerbric.pride.PrideClient;
import io.github.queerbric.pride.PrideFlag;
import io.github.queerbric.pride.PrideFlags;

public class PrideFlagProvider {
	public static PrideFlagRenderer get() {
		if (PrideFlags.isPrideMonth()){
			PrideFlag ret = PrideFlags.getRandomFlag();
			if (ret != null) return (dc, x, y, w, h) -> ret.render(dc.getMatrices(), x, y, w, h);
		}
		return null;
	}

	public static void init() {
		new PrideClient().onInitializeClient();
	}
}
