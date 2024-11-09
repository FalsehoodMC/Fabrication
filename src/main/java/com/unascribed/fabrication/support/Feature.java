package com.unascribed.fabrication.support;

public interface Feature {
	void apply();
	boolean undo();
	String getConfigKey();
}
