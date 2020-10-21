package com.unascribed.fabrication.support;

import java.util.Locale;

public enum Trilean {
	UNSET,
	TRUE,
	FALSE;
	
	public boolean resolve(boolean def) {
		if(this == TRUE) return true;
		if(this == FALSE) return false;
		return def;
	}
	
	public Trilean not() {
		if (this==TRUE) return FALSE;
		if (this==FALSE) return TRUE;
		return UNSET;
	}
	
	public static Trilean parseTrilean(String s) {
		return valueOf(s.toUpperCase(Locale.ROOT));
	}
}
