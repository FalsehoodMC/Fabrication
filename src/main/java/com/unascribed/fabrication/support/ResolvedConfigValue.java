package com.unascribed.fabrication.support;

public enum ResolvedConfigValue {
	TRUE(true, ConfigValue.TRUE),
	FALSE(false, ConfigValue.FALSE),
	DEFAULT_TRUE(true, ConfigValue.UNSET),
	DEFAULT_FALSE(false, ConfigValue.UNSET),
	BANNED(false, ConfigValue.BANNED),
	;

	public final boolean value;
	public final ConfigValue trilean;

	ResolvedConfigValue(boolean value, ConfigValue trilean) {
		this.value = value;
		this.trilean = trilean;
	}
}
