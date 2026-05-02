package com.unascribed.fabrication.interfaces;

import com.unascribed.fabrication.support.ConfigValues;

import java.util.Map;
import java.util.Set;

public interface GetServerConfig {

	boolean fabrication$hasHandshook();

	String fabrication$getServerVersion();
	Map<String, ConfigValues.ResolvedFeature> fabrication$getServerTrileanConfig();
	Map<String, String> fabrication$getServerStringConfig();
	Map<String, String> fabrication$getServerFailedConfig();
	Set<String> fabrication$getServerBanned();

	long fabrication$getLaunchId();

}
