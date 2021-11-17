package com.unascribed.fabrication.support;

import java.nio.file.Path;

import com.unascribed.fabrication.QDIni;

public interface ConfigLoader {

	void load(Path configDir, QDIni config, boolean loadError);

	String getConfigName();

}
