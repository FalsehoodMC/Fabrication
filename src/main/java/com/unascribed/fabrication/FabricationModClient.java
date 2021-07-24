package com.unascribed.fabrication;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;

public class FabricationModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricationClientCommands.registerCommands(ClientCommandManager.DISPATCHER);
    }
}
