package com.unascribed.fabrication.support;

public enum SpecialEligibility {
	NEVER("Never"),
	NOT_FORGE("Running under Fabric"),
	FORGE("Running under Forge"),
	NO_OPTIFINE("OptiFine is not present")
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
