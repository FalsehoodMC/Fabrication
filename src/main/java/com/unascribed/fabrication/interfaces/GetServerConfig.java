package com.unascribed.fabrication.interfaces;

import java.util.Map;
import java.util.Set;

import com.unascribed.fabrication.support.ConfigValues;

public interface GetServerConfig {

	boolean fabrication$hasHandshook();

	String fabrication$getServerVersion();
	Map<String, ConfigValues.ResolvedFeature> fabrication$getServerTrileanConfig();
	Map<String, String> fabrication$getServerStringConfig();
	Set<String> fabrication$getServerFailedConfig();
	Set<String> fabrication$getServerBanned();

	long fabrication$getLaunchId();

}
