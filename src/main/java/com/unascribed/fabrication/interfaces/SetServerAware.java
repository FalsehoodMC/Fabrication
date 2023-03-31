package com.unascribed.fabrication.interfaces;

import net.minecraft.server.MinecraftServer;

public interface SetServerAware {
	void fabrication$pingSetServer(MinecraftServer server);
}
