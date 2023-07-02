package com.unascribed.fabrication.util.forgery_nonsense;

import net.minecraft.server.ServerMetadata;

public class ForgeryServerMetadata {
	//This exists because forgery is jank
	public static ServerMetadata get() {
		return new ServerMetadata();
	}
}
