package com.unascribed.fabrication.features;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DifficultyCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.level.ServerWorldProperties;

@EligibleIf(configAvailable="*.legacy_command_syntax")
public class FeatureLegacyCommandSyntax implements Feature {

	private boolean applied = false;

	@Override
	public void apply() {
		if (applied) return;
		applied = true;
		Agnos.runForCommandRegistration((dispatcher, dedi) -> {
			try {
				LiteralArgumentBuilder<ServerCommandSource> gmCmd = CommandManager.literal("gamemode")
						.requires(scs -> FabConf.isEnabled("*.legacy_command_syntax") && scs.hasPermissionLevel(2));
				for (GameMode mode : GameMode.values()) {
					if (mode == GameMode.NOT_SET) continue;
					gmCmd.then(CommandManager.literal(Integer.toString(mode.getId()))
							.executes(c -> FabRefl.gameModeExecute(c, Collections.singleton(c.getSource().getPlayer()), mode))
							.then(CommandManager.argument("target", EntityArgumentType.players())
									.executes(c -> FabRefl.gameModeExecute(c, EntityArgumentType.getPlayers(c, "target"), mode)))
							);
				}
				dispatcher.register(gmCmd);

				LiteralArgumentBuilder<ServerCommandSource> diffCmd = CommandManager.literal("difficulty")
						.requires(scs -> FabConf.isEnabled("*.legacy_command_syntax") && scs.hasPermissionLevel(2));
				for (Difficulty difficulty : Difficulty.values()) {
					diffCmd.then(CommandManager.literal(Integer.toString(difficulty.getId()))
							.executes(c -> DifficultyCommand.execute(c.getSource(), difficulty)));
				}
				dispatcher.register(diffCmd);

				dispatcher.register(CommandManager.literal("experience")
						.requires(scs -> FabConf.isEnabled("*.legacy_command_syntax") && scs.hasPermissionLevel(2))
						.then(CommandManager.argument("amount", IntegerArgumentType.integer())
								.executes(c -> {
									return addExperience(c.getSource(), Collections.singleton(c.getSource().getPlayer()), IntegerArgumentType.getInteger(c,"amount"), false);
								})
								.then(CommandManager.argument("targets", EntityArgumentType.players())
										.executes(c -> {
											return addExperience(c.getSource(), EntityArgumentType.getPlayers(c, "targets"), IntegerArgumentType.getInteger(c, "amount"), false);
										})
										)
								)
						.then(CommandManager.argument("lvlAmount", StringArgumentType.word())
								.executes((c) -> {
									return addExperience(c.getSource(), Collections.singleton(c.getSource().getPlayer()), StringArgumentType.getString(c, "lvlAmount"));
								})
								.then(CommandManager.argument("targets", EntityArgumentType.players())
										.executes(c ->{
											return addExperience(c.getSource(), EntityArgumentType.getPlayers(c, "targets"), StringArgumentType.getString(c, "lvlAmount"));
										})
										)
								));

				dispatcher.register(CommandManager.literal("toggledownfall")
						.requires(scs -> FabConf.isEnabled("*.legacy_command_syntax") && scs.hasPermissionLevel(2))
						.executes(c -> {
							ServerWorld world = c.getSource().getWorld();
							ServerWorldProperties props = (ServerWorldProperties) FabRefl.getWorldProperties(world);
							if (props.isRaining()) {
								world.setWeather(12000, 0, false, props.isThundering());
							} else {
								world.setWeather(0, 12000, true, props.isThundering());
							}
							c.getSource().sendFeedback(new LiteralText("Toggled downfall"), true);
							return 1;
						}));
			} catch (Throwable t) {
				FabricationMod.featureError(this, t);
			}
		});
	}

	private static int addExperience(ServerCommandSource source, Collection<? extends ServerPlayerEntity> targets, int amount, boolean areLevels) {
		Iterator<? extends ServerPlayerEntity> iter = targets.iterator();
		while (iter.hasNext()) {
			ServerPlayerEntity p = iter.next();
			if (areLevels) {
				p.addExperienceLevels(amount);
			} else {
				p.addExperience(amount);
			}
		}
		String thing = (areLevels ? "levels" : "points");
		if (targets.size() == 1) {
			source.sendFeedback(new TranslatableText("commands.experience.add."+thing+".success.single", amount, Iterables.getOnlyElement(targets).getDisplayName()), true);
		} else {
			source.sendFeedback(new TranslatableText("commands.experience.add."+thing+".success.multiple", amount, targets.size()), true);
		}
		return targets.size();
	}

	private static int addExperience(ServerCommandSource source, Collection<? extends ServerPlayerEntity> targets, String amount) throws CommandSyntaxException{
		if (amount.endsWith("l") || amount.endsWith("L")) {
			Integer i = Ints.tryParse(amount.substring(0, amount.length()-1));
			if (i != null) {
				return addExperience(source, targets, i, true);
			}
		}
		throw new SimpleCommandExceptionType(new LiteralText("Invalid XP amount")).create();
	}

	@Override
	public boolean undo() {
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.legacy_command_syntax";
	}

}
