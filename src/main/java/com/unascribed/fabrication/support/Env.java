package com.unascribed.fabrication.support;

public enum Env {
	ANY,
	CLIENT,
	SERVER,
	;

	public boolean matches(Env e) {
		return this == ANY || e == ANY || e == this;
	}
}
