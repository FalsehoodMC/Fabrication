package com.unascribed.fabrication.client;

import com.unascribed.fabrication.FabConf;
import com.unascribed.lib39.deferral.api.GLCompatVoter;

public class FabricationGLCompatVoter implements GLCompatVoter {

	@Override
	public boolean wantsCompatibilityProfile() {
		return FabConf.isEnabled("*.end_portal_parallax");
	}
	
}
