package com.unascribed.fabrication;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.util.DyeColor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public class FabricationModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricationClientCommands.registerCommands(ClientCommandManager.DISPATCHER);
    }
}
