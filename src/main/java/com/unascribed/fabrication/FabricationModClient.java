package com.unascribed.fabrication;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import com.unascribed.fabrication.interfaces.GetServerConfig;

public class FabricationModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricationClientCommands.registerCommands(ClientCommandManager.DISPATCHER);
    }

	public static boolean isBannedByServer(String configKey) {
		if (MinecraftClient.getInstance() == null) return false;
		ClientPlayNetworkHandler net = MinecraftClient.getInstance().getNetworkHandler();
		if (net != null && net instanceof GetServerConfig) {
			return ((GetServerConfig)net).fabrication$getServerBanned().contains(configKey);
		}
		return false;
	}
}
