package com.unascribed.fabrication;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.features.FeatureFabricationCommand;

import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;

public class FabricationClientCommands implements ClientCommandPlugin {

	@Override
	public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {
		// using the client command in singleplayer makes no sense
		if (MinecraftClient.getInstance().getServer() == null) {
			LiteralArgumentBuilder<CottonClientCommandSource> root = LiteralArgumentBuilder.<CottonClientCommandSource>literal("fabrication:client");
			FeatureFabricationCommand.addConfig(root);
			dispatcher.register(root);
		}
	}

	public static void sendFeedback(CommandContext<? extends CommandSource> c, LiteralText text) {
		((CottonClientCommandSource)c.getSource()).sendFeedback(new LiteralText("§b[CLIENT]§r ").append(text));
	}

}
