package com.unascribed.fabrication.support;

import java.util.Map;

public interface ConfigLoader {

	void load(Map<String, String> config);
	
	String getConfigName();
	
}
