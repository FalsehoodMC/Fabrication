package com.unascribed.fabrication;

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
import com.unascribed.fabrication.client.FabricationConfigScreen;
import com.unascribed.fabrication.client.OptionalFScriptScreen;
import com.unascribed.fabrication.client.OptionalPrideFlag;
import com.unascribed.fabrication.features.FeatureFabricationCommand;
import com.unascribed.fabrication.support.OptionalFScript;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class FabricationClientCommands {

	public static class AtlasArgumentType implements ArgumentType<Identifier> {

		@Override
		public Identifier parse(StringReader reader) throws CommandSyntaxException {
			Identifier id = Identifier.fromCommandInput(reader);
			for (SpriteAtlasTexture sat : AtlasTracking.allAtlases) {
				if (sat.getId().equals(id)) return id;
			}
			throw new CommandException(Text.literal("There is no atlas with ID " + id));
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
	private static final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
	public static String rootCommand = FabricationMod.MOD_NAME_LOWER+":client";
	public static void registerCommands() {
		registerCommands(dispatcher);
	}
	public static boolean runCommand(String command) {
		if (command.isEmpty() || !command.startsWith(rootCommand)) return false;
		try {
			dispatcher.execute(command, MinecraftClient.getInstance().getNetworkHandler().getCommandSource());
		} catch (CommandException ignore) {
		} catch (Exception e) {
			FabLog.error("Failed to execute client command: "+command, e);
			sendFeedback(Text.literal("§c"+e));
		}
		return true;
	}
	public static<T extends CommandSource> void addSuggestions(CommandDispatcher<T> dispatcher) {
		registerCommands(dispatcher);
	}
	private static<T extends CommandSource> void registerCommands(CommandDispatcher<T> dispatcher) {
		LiteralArgumentBuilder<T> root = LiteralArgumentBuilder.<T>literal(rootCommand);
		if (EarlyAgnos.isModLoaded("fscript")) addFScript(root);
		FeatureFabricationCommand.addConfig(root, false);
		root.then(LiteralArgumentBuilder.<T>literal("ui")
				.executes((c) -> {
					MinecraftClient.getInstance().send(() -> {
						MinecraftClient.getInstance().setScreen(new FabricationConfigScreen(null));
					});
					return 1;
				}));
		if (!FabConf.isFailed("atlas_viewer")) {
			root.then(LiteralArgumentBuilder.<T>literal("atlas")
					.then(LiteralArgumentBuilder.<T>literal("view")
							.then(RequiredArgumentBuilder.<T, Identifier>argument("atlas", new AtlasArgumentType())
									.executes((c) -> {
										MinecraftClient.getInstance().send(() -> {
											MinecraftClient.getInstance().setScreen(new AtlasViewerScreen(c.getArgument("atlas", Identifier.class)));
										});
										return 1;
									}))));
		}
		dispatcher.register(root);
	}
	public static <T extends CommandSource> void addFScript(LiteralArgumentBuilder<T> root) {
		LiteralArgumentBuilder<T> script = LiteralArgumentBuilder.<T>literal("fscript");
		{
			LiteralArgumentBuilder<T> ui = LiteralArgumentBuilder.<T>literal("ui");
			for (String s : OptionalFScript.predicateProviders.keySet()) {
				LiteralArgumentBuilder<T> key = LiteralArgumentBuilder.<T>literal(s).executes((c) -> {
					MinecraftClient.getInstance().send(() -> {
						MinecraftClient.getInstance().setScreen(OptionalFScriptScreen.construct(null, OptionalPrideFlag.get(), FeaturesFile.get(s).name, s));
					});
					return 1;
				});
				ui.then(key);
				if (s.contains("."))
					ui.then(LiteralArgumentBuilder.<T>literal("*"+s.substring(s.indexOf('.'))).executes(key.getCommand()));
			}
			script.then(ui);
		}
		root.then(script);
	}
	public static void sendFeedback(MutableText text) {
		MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("§b[CLIENT]§r ").append(text));
	}

}
