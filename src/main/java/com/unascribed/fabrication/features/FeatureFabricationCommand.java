package com.unascribed.fabrication.features;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.OnlyIf;
import com.unascribed.fabrication.support.Trilean;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

@OnlyIf(dependencies="fabric")
public class FeatureFabricationCommand implements Feature {

	public static class ConfigArgumentType implements ArgumentType<String> {
		private static final Collection<String> EXAMPLES = Arrays.asList("tweaks.endermen_dont_squeal", "tweaks.tridents_in_void_return");
		public static final DynamicCommandExceptionType INVALID_KEY_EXCEPTION = new DynamicCommandExceptionType(arg -> {
			return new LiteralText("Invalid config key "+arg);
		});

		@Override
		public String parse(final StringReader stringReader) throws CommandSyntaxException {
			String str = stringReader.readUnquotedString();
			if (!MixinConfigPlugin.isValid(str)) throw INVALID_KEY_EXCEPTION.create(str);
			return str;
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			return CommandSource.suggestMatching(MixinConfigPlugin.getAllKeys(), builder);
		}

		@Override
		public Collection<String> getExamples() {
			return ConfigArgumentType.EXAMPLES;
		}

	}
	
	public static class TrileanArgumentType implements ArgumentType<Trilean> {
		private static final Collection<String> EXAMPLES = Arrays.asList("true", "false", "unset");
		public static final DynamicCommandExceptionType INVALID_TRILEAN_EXCEPTION = new DynamicCommandExceptionType(arg -> {
			return new LiteralText("Invalid trilean "+arg);
		});

		@Override
		public Trilean parse(final StringReader stringReader) throws CommandSyntaxException {
			String str = stringReader.readUnquotedString();
			try {
				return Trilean.parseTrilean(str);
			} catch (IllegalArgumentException e) {
				throw INVALID_TRILEAN_EXCEPTION.create(str);
			}
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			return CommandSource.suggestMatching(EXAMPLES, builder);
		}

		@Override
		public Collection<String> getExamples() {
			return ConfigArgumentType.EXAMPLES;
		}

	}
	
	@Override
	public void apply() {
		ConfigArgumentType cat = new ConfigArgumentType();
		ArgumentTypes.register("fabrication:config", ConfigArgumentType.class, new ConstantArgumentSerializer<>(() -> cat));
		TrileanArgumentType tat = new TrileanArgumentType();
		ArgumentTypes.register("fabrication:trilean", TrileanArgumentType.class, new ConstantArgumentSerializer<>(() -> tat));
		CommandRegistrationCallback.EVENT.register((dispatcher, dedi) -> {
			dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("fabrication")
				.requires(scs -> scs.hasPermissionLevel(4))
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("config")
					.then(LiteralArgumentBuilder.<ServerCommandSource>literal("get")
						.then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("key", cat)
							.executes((c) -> {
								String key = c.getArgument("key", String.class);
								Trilean value = MixinConfigPlugin.getValue(key);
								boolean def = MixinConfigPlugin.getDefault(key);
								String valueLower = value.name().toLowerCase(Locale.ROOT);
								c.getSource().sendFeedback(new LiteralText(key+" = "+valueLower+" (default "+def+")"), false);
								return 1;
							})
						)
						.executes(c -> {
							return 0;
						})
					)
					.then(LiteralArgumentBuilder.<ServerCommandSource>literal("set")
						.then(RequiredArgumentBuilder.<ServerCommandSource, String>argument("key", cat)
							.then(RequiredArgumentBuilder.<ServerCommandSource, Trilean>argument("value", tat)
								.executes((c) -> {
									String key = c.getArgument("key", String.class);
									Trilean oldValue = MixinConfigPlugin.getValue(key);
									Trilean value = c.getArgument("value", Trilean.class);
									String valueLower = value.name().toLowerCase(Locale.ROOT);
									boolean def = MixinConfigPlugin.getDefault(key);
									if (value == oldValue) {
										c.getSource().sendFeedback(new LiteralText(key+" is already set to "+valueLower+" (default "+def+")"), false);
									} else {
										MixinConfigPlugin.set(key, value);
										c.getSource().sendFeedback(new LiteralText(key+" is now set to "+valueLower+" (default "+def+")"), true);
										if (FabricationMod.isAvailableFeature(key)) {
											if (FabricationMod.updateFeature(key)) {
												return 1;
											}
										}
										if ("general.runtime_checks".equals(key)) {
											c.getSource().sendFeedback(new LiteralText("§cYou will need to restart the game for this change to take effect."), false);
										} else if (!RuntimeChecks.ENABLED) {
											c.getSource().sendFeedback(new LiteralText("§cgeneral.runtime_checks is disabled, you will need to restart the game for this change to take effect."), false);
										}
									}
									return 1;
								})
							)
						)
					)
					.then(LiteralArgumentBuilder.<ServerCommandSource>literal("reload")
						.executes((c) -> {
							MixinConfigPlugin.reload();
							c.getSource().sendFeedback(new LiteralText("fabrication.ini reloaded"), true);
							c.getSource().sendFeedback(new LiteralText("§eYou may need to restart the game for the changes to take effect."), false);
							return 1;
						})
					)
				)
			);
			try {
				// I mean, you never know...
				Class.forName("org.bukkit.Bukkit");
			} catch (Throwable t) {
				dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("plugins")
						.executes((c) -> {
							c.getSource().sendFeedback(new LiteralText("§cThis ain't no Bukkit!\nTry /mods"), false);
							return 1;
						}));
			}
		});
	}

	@Override
	public boolean undo() {
		return false;
	}

	@Override
	public String getConfigKey() {
		return null;
	}

}
