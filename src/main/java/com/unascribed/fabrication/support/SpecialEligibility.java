package com.unascribed.fabrication.support;

public enum SpecialEligibility {
	NEVER("Never"),
	NOT_FORGE("Running under Fabric"),
	FORGE("Running under Forge"),
	NO_OPTIFINE("OptiFine is not present"),
	NOT_MACOS("Not running under macOS"),
	NOT_1191("Not running 1.19.1 or newer")
	;
	public final String displayName;

	SpecialEligibility(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return "\""+displayName+"\"";
	}
}
