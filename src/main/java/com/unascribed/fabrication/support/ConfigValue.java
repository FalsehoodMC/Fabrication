package com.unascribed.fabrication.support;

import java.util.Locale;

public enum ConfigValue {
	UNSET,
	TRUE,
	FALSE,
	BANNED;
	
	public boolean resolve(boolean def) {
		if (this == TRUE) return true;
		if (this == FALSE || this == BANNED) return false;
		return def;
	}
	
	public ResolvedConfigValue resolveSemantically(boolean def) {
		if (this == BANNED) return ResolvedConfigValue.BANNED;
		if (this == TRUE) return ResolvedConfigValue.TRUE;
		if (this == FALSE) return ResolvedConfigValue.FALSE;
		return def ? ResolvedConfigValue.DEFAULT_TRUE : ResolvedConfigValue.DEFAULT_FALSE;
	}
	
	public static ConfigValue parseTrilean(String s) {
		return valueOf(s.toUpperCase(Locale.ROOT));
	}
}
