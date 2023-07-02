package com.unascribed.fabrication.util.forgery_nonsense;

import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;

import java.util.Optional;

public class ForgeryServerMetadata {
	//This exists because forgery is jank
	public static ServerMetadata get(Text description, Optional<ServerMetadata.Players> players, Optional<ServerMetadata.Version> version, Optional<ServerMetadata.Favicon> favicon, boolean secureChatEnforced) {
		return new ServerMetadata(description, players, version, favicon, secureChatEnforced);
	}
}
