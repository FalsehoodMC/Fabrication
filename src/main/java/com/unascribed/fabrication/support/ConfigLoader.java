package com.unascribed.fabrication.support;

import java.nio.file.Path;
import java.util.Map;

public interface ConfigLoader {

	void load(Path configDir, Map<String, String> config);
	
	String getConfigName();
	
}
