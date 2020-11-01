package com.unascribed.fabrication.interfaces;

import java.util.Map;

import com.unascribed.fabrication.support.ResolvedTrilean;

public interface GetServerConfig {

	boolean fabrication$hasHandshook();
	
	Map<String, ResolvedTrilean> fabrication$getServerTrileanConfig();
	Map<String, String> fabrication$getServerStringConfig();
	
}
