package com.unascribed.fabrication.support;

public enum ResolvedTrilean {
	TRUE(true, Trilean.TRUE),
	FALSE(false, Trilean.FALSE),
	DEFAULT_TRUE(true, Trilean.UNSET),
	DEFAULT_FALSE(false, Trilean.UNSET),
	;
	
	public final boolean value;
	public final Trilean trilean;
	
	ResolvedTrilean(boolean value, Trilean trilean) {
		this.value = value;
		this.trilean = trilean;
	}
}
