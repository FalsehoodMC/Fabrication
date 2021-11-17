package com.unascribed.fabrication.interfaces;

import java.util.Map;
import java.util.Set;

import com.unascribed.fabrication.support.ResolvedTrilean;

public interface GetServerConfig {

	boolean fabrication$hasHandshook();

	String fabrication$getServerVersion();
	Map<String, ResolvedTrilean> fabrication$getServerTrileanConfig();
	Map<String, String> fabrication$getServerStringConfig();
	Set<String> fabrication$getServerFailedConfig();

	long fabrication$getLaunchId();

}
