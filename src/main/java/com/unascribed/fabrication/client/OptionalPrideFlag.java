package com.unascribed.fabrication.client;

import com.unascribed.fabrication.EarlyAgnos;
import com.unascribed.fabrication.FabLog;

public class OptionalPrideFlag {
	private static boolean ok = EarlyAgnos.isModLoaded("pridelib");
	private static boolean first = true;
	public static PrideFlagRenderer get() {
		if (ok) try {
			if (first && EarlyAgnos.isForge()){
				PrideFlagProvider.init();
				first = false;
			}
			return PrideFlagProvider.get();
		} catch (Throwable th) {
			FabLog.error("PrideLib was present but failed to load", th);
			ok = false;
		}
		return null;
	}
}
