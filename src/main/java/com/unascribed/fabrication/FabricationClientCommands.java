package com.unascribed.fabrication;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.unascribed.fabrication.client.AtlasTracking;
import com.unascribed.fabrication.client.AtlasViewerScreen;
import com.unascribed.fabrication.features.FeatureFabricationCommand;

import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class FabricationClientCommands implements ClientCommandPlugin {

	public static class AtlasArgumentType implements ArgumentType<Identifier> {

		@Override
		public Identifier parse(StringReader reader) throws CommandSyntaxException {
			Identifier id = Identifier.fromCommandInput(reader);
			for (SpriteAtlasTexture sat : AtlasTracking.allAtlases) {
				if (sat.getId().equals(id)) return id;
			}
			throw new CommandException(new LiteralText("There is no atlas with ID "+id));
		}
		
		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			for (SpriteAtlasTexture sat : AtlasTracking.allAtlases) {
				builder.suggest(sat.getId().toString());
			}
			return builder.buildFuture();
		}
		
		@Override
		public Collection<String> getExamples() {
			return Collections.singleton(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE.toString());
		}
	}
	
	@Override
	public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {
		LiteralArgumentBuilder<CottonClientCommandSource> root = LiteralArgumentBuilder.<CottonClientCommandSource>literal("fabrication:client");
		FeatureFabricationCommand.addConfig(root);
		root.then(LiteralArgumentBuilder.<CottonClientCommandSource>literal("atlas")
				.then(LiteralArgumentBuilder.<CottonClientCommandSource>literal("view")
						.then(RequiredArgumentBuilder.<CottonClientCommandSource, Identifier>argument("atlas", new AtlasArgumentType())
								.executes((c) -> {
									MinecraftClient.getInstance().send(() -> {
										MinecraftClient.getInstance().openScreen(new AtlasViewerScreen(c.getArgument("atlas", Identifier.class)));
									});
									return 1;
								}))));
		dispatcher.register(root);
	}

	public static void sendFeedback(CommandContext<? extends CommandSource> c, LiteralText text) {
		((CottonClientCommandSource)c.getSource()).sendFeedback(new LiteralText("§b[CLIENT]§r ").append(text));
	}

}
