package com.unascribed.fabrication.client;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.unascribed.fabrication.features.FeatureFabricationCommand;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import io.github.fablabsmc.fablabs.api.client.command.v1.ClientCommandRegistrationCallback;
import io.github.fablabsmc.fablabs.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class FabricationModClient implements ClientModInitializer {

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
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.event().register((dispatcher) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.<FabricClientCommandSource>literal("fabrication:client");
			FeatureFabricationCommand.addConfig(root, false);
			if (!MixinConfigPlugin.isFailed("atlas_viewer")) {
				root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("atlas")
						.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("view")
								.then(RequiredArgumentBuilder.<FabricClientCommandSource, Identifier>argument("atlas", new AtlasArgumentType())
										.executes((c) -> {
											MinecraftClient.getInstance().send(() -> {
												MinecraftClient.getInstance().openScreen(new AtlasViewerScreen(c.getArgument("atlas", Identifier.class)));
											});
											return 1;
										}))));
			}
			dispatcher.register(root);
		});
	}

	public static void sendFeedback(CommandContext<? extends CommandSource> c, LiteralText text) {
		((FabricClientCommandSource)c.getSource()).sendFeedback(new LiteralText("§b[CLIENT]§r ").append(text));
	}
	
}
