package com.unascribed.fabrication.interfaces;

import java.util.Map;
import java.util.Set;

import com.unascribed.fabrication.support.ResolvedConfigValue;

public interface GetServerConfig {

	boolean fabrication$hasHandshook();

	String fabrication$getServerVersion();
	Map<String, ResolvedConfigValue> fabrication$getServerTrileanConfig();
	Map<String, String> fabrication$getServerStringConfig();
	Set<String> fabrication$getServerFailedConfig();
	Set<String> fabrication$getServerBanned();

	long fabrication$getLaunchId();

}
