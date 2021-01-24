package com.unascribed.fabrication.support;

public enum SpecialEligibility {
	NEVER("Never", false),
	EVENTS_AVAILABLE("Running under Fabric with Fabric API, or running under Forge", false),
	NOT_FORGE("Running under Fabric", false),
	FORGE("Running under Forge", false),
	NO_OPTIFINE("OptiFine is not present", false)
	;
	public final String displayName;
	public final boolean ignorableWithRuntimeChecks;
	private SpecialEligibility(String displayName, boolean ignorableWithRuntimeChecks) {
		this.displayName = displayName;
		this.ignorableWithRuntimeChecks = ignorableWithRuntimeChecks;
	}
	
	@Override
	public String toString() {
		return "\""+displayName+"\"";
	}
}
