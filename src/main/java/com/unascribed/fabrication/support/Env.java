package com.unascribed.fabrication.support;

import net.fabricmc.api.EnvType;

public enum Env {
	ANY(null),
	CLIENT(EnvType.CLIENT),
	SERVER(EnvType.SERVER),
	;
	public final EnvType fabric;
	private Env(EnvType fabric) {
		this.fabric = fabric;
	}
}
