package com.unascribed.fabrication.support;

import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.Agnos;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;

import java.util.HashSet;
import java.util.Set;

public class FabricationEvents {
	private static final Set<Agnos.CommandRegistrationCallback> commands = new HashSet<>();

	public static void addCommand(Agnos.CommandRegistrationCallback c) {
		commands.add(c);
	}

	public static void commands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
		for (Agnos.CommandRegistrationCallback c : commands) {
			c.register(dispatcher, commandRegistryAccess, true);
		}
	}
}
