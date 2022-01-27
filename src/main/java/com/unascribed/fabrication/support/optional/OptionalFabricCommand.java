package com.unascribed.fabrication.support.optional;

import com.unascribed.fabrication.Agnos;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

@Environment(EnvType.CLIENT)
public class OptionalFabricCommand {
	public static void runForCommandRegistration(Agnos.CommandRegistrationCallback r) {
		CommandRegistrationCallback.EVENT.register(r::register);
	}
}
