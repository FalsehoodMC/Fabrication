package com.unascribed.fabrication.features;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.FabricationClientCommands;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.PlayerTag;
import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.Profile;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

@EligibleIf(modLoaded="fabric")
public class FeatureFabricationCommand implements Feature {
	
	@Override
	public void apply() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedi) -> {
			LiteralArgumentBuilder<ServerCommandSource> root = LiteralArgumentBuilder.<ServerCommandSource>literal("fabrication");
			addConfig(root);
			
			LiteralArgumentBuilder<ServerCommandSource> tag = LiteralArgumentBuilder.<ServerCommandSource>literal("tag");
			tag.requires(scs -> MixinConfigPlugin.isEnabled("*.taggable_players") && scs.hasPermissionLevel(2));
			{
				LiteralArgumentBuilder<ServerCommandSource> add = LiteralArgumentBuilder.<ServerCommandSource>literal("add");
				LiteralArgumentBuilder<ServerCommandSource> remove = LiteralArgumentBuilder.<ServerCommandSource>literal("remove");
				LiteralArgumentBuilder<ServerCommandSource> get = LiteralArgumentBuilder.<ServerCommandSource>literal("get");
				LiteralArgumentBuilder<ServerCommandSource> clear = LiteralArgumentBuilder.<ServerCommandSource>literal("clear");
				
				for (PlayerTag pt : PlayerTag.values()) {
					add.then(CommandManager.literal(pt.lowerName())
						.executes(c -> {
							return addTag(c, Collections.singleton(c.getSource().getPlayer()), pt);
						})
						.then(CommandManager.argument("players", EntityArgumentType.players())
							.executes(c -> {
								return addTag(c, EntityArgumentType.getPlayers(c, "players"), pt);
							})
						)
					);
					
					remove.then(CommandManager.literal(pt.lowerName())
						.executes(c -> {
							return removeTag(c, Collections.singleton(c.getSource().getPlayer()), pt);
						})
						.then(CommandManager.argument("players", EntityArgumentType.players())
							.executes(c -> {
								return removeTag(c, EntityArgumentType.getPlayers(c, "players"), pt);
							})
						)
					);
				}
				
				get.executes(c -> {
					return getTags(c, c.getSource().getPlayer());
				}).then(CommandManager.argument("player", EntityArgumentType.player())
					.executes(c -> {
						return getTags(c, EntityArgumentType.getPlayer(c, "player"));
					})
				);
				
				clear.executes(c -> {
					return clearTags(c, Collections.singleton(c.getSource().getPlayer()));
				}).then(CommandManager.argument("players", EntityArgumentType.players())
					.executes(c -> {
						return clearTags(c, EntityArgumentType.getPlayers(c, "players"));
					})
				);
			
				tag.then(add);
				tag.then(remove);
				tag.then(get);
				tag.then(clear);
				
			}
			root.then(tag);
			
			dispatcher.register(root);
		});
	}

	public static <T extends CommandSource> void addConfig(LiteralArgumentBuilder<T> root) {
		LiteralArgumentBuilder<T> config = LiteralArgumentBuilder.<T>literal("config");
		config.requires(s -> {
			// always allow a client to reconfigure itself
			if (!(s instanceof ServerCommandSource)) return true;
			
			ServerCommandSource scs = (ServerCommandSource)s;
			if (scs.hasPermissionLevel(4)) return true;
			if (scs.getMinecraftServer().isSinglePlayer() && scs.getEntity() != null) {
				Entity e = scs.getEntity();
				if (e instanceof PlayerEntity) {
					if (scs.getMinecraftServer().getUserName().equals(((PlayerEntity)e).getGameProfile().getName())) {
						// always allow in singleplayer, even if cheats are off
						return true;
					}
				}
			}
			return false;
		});
		{
			LiteralArgumentBuilder<T> get = LiteralArgumentBuilder.<T>literal("get");
			for (String s : MixinConfigPlugin.getAllKeys()) {
				LiteralArgumentBuilder<T> key = LiteralArgumentBuilder.<T>literal(s);
				key.executes((c) -> {
					String value = MixinConfigPlugin.getRawValue(s);
					boolean tri = MixinConfigPlugin.isTrilean(s);
					if (value.isEmpty() && tri) value = "unset";
					boolean def = MixinConfigPlugin.getDefault(s);
					sendFeedback(c, new LiteralText(s+" = "+value+(tri ? " (default "+def+")" : "")), false);
					return 1;
				});
				get.then(key);
			}
			config.then(get);
			LiteralArgumentBuilder<T> set = LiteralArgumentBuilder.<T>literal("set");
			for (String s : MixinConfigPlugin.getAllKeys()) {
				LiteralArgumentBuilder<T> key = LiteralArgumentBuilder.<T>literal(s);
				String[] values;
				if (s.equals("general.runtime_checks")) {
					values = new String[]{"true", "false"};
				} else if (s.equals("general.profile")) {
					values = Profile.stringValues();
				} else {
					values = new String[]{"unset", "true", "false"};
				}
				for (String v : values) {
					key.then(LiteralArgumentBuilder.<T>literal(v)
							.executes((c) -> {
								setKeyWithFeedback(c, s, v);
								return 1;
							}));
				}
				set.then(key);
			}
			config.then(set);
			config.then(LiteralArgumentBuilder.<T>literal("reload")
				.executes((c) -> {
					MixinConfigPlugin.reload();
					FeatureItemDespawn.parseConfig();
					if (c.getSource() instanceof ServerCommandSource) {
						FabricationMod.sendConfigUpdate(((ServerCommandSource)c.getSource()).getMinecraftServer(), null);
					}
					sendFeedback(c, new LiteralText("Fabrication configuration reloaded"), true);
					sendFeedback(c, new LiteralText("§eYou may need to restart the game for the changes to take effect."), false);
					return 1;
				})
			);
		}
		root.then(config);
	}

	private static void sendFeedback(CommandContext<? extends CommandSource> c, LiteralText text, boolean broadcast) {
		if (c.getSource() instanceof ServerCommandSource) {
			((ServerCommandSource)c.getSource()).sendFeedback(text, broadcast);
		} else {
			sendFeedbackCotton(c, text);
		}
	}

	private static void sendFeedbackCotton(CommandContext<? extends CommandSource> c, LiteralText text) {
		FabricationClientCommands.sendFeedback(c, text);
	}

	private int clearTags(CommandContext<ServerCommandSource> c, Collection<ServerPlayerEntity> players) {
		for (ServerPlayerEntity spe : players) {
			((TaggablePlayer)spe).fabrication$clearTags();
			c.getSource().sendFeedback(new LiteralText("Cleared tags for ").append(spe.getDisplayName()), true);
		}
		return 1;
	}

	private int getTags(CommandContext<ServerCommandSource> c, ServerPlayerEntity player) {
		LiteralText lt = new LiteralText("Tags: ");
		Set<PlayerTag> tags = ((TaggablePlayer)player).fabrication$getTags();
		if (tags.isEmpty()) {
			lt.append("none");
		} else {
			lt.append(Joiner.on(", ").join(Collections2.transform(tags, PlayerTag::lowerName)));
		}
		c.getSource().sendFeedback(lt, false);
		return 1;
	}

	private int addTag(CommandContext<ServerCommandSource> c, Collection<ServerPlayerEntity> players, PlayerTag pt) {
		for (ServerPlayerEntity spe : players) {
			((TaggablePlayer)spe).fabrication$setTag(pt, true);
			c.getSource().sendFeedback(new LiteralText("Added tag "+pt.lowerName()+" to ").append(spe.getDisplayName()), true);
		}
		return 1;
	}
	
	private int removeTag(CommandContext<ServerCommandSource> c, Collection<ServerPlayerEntity> players, PlayerTag pt) {
		for (ServerPlayerEntity spe : players) {
			((TaggablePlayer)spe).fabrication$setTag(pt, false);
			c.getSource().sendFeedback(new LiteralText("Removed tag "+pt.lowerName()+" from ").append(spe.getDisplayName()), true);
		}
		return 1;
	}

	private static void setKeyWithFeedback(CommandContext<? extends CommandSource> c, String key, String value) {
		String oldValue = MixinConfigPlugin.getRawValue(key);
		boolean def = MixinConfigPlugin.getDefault(key);
		boolean tri = MixinConfigPlugin.isTrilean(key);
		if (value.equals(oldValue)) {
			sendFeedback(c, new LiteralText(key+" is already set to "+value+(tri ? " (default "+def+")" : "")), false);
		} else {
			MixinConfigPlugin.set(key, value);
			if (c.getSource() instanceof ServerCommandSource) {
				FabricationMod.sendConfigUpdate(((ServerCommandSource)c.getSource()).getMinecraftServer(), key);
			}
			sendFeedback(c, new LiteralText(key+" is now set to "+value+(tri ? " (default "+def+")" : "")), true);
			if (FabricationMod.isAvailableFeature(key)) {
				if (FabricationMod.updateFeature(key)) {
					return;
				}
			}
			if ("general.runtime_checks".equals(key)) {
				sendFeedback(c, new LiteralText("§cYou will need to restart the game for this change to take effect."), false);
			} else if (!RuntimeChecks.ENABLED && !MixinConfigPlugin.isRuntimeConfigurable(key)) {
				sendFeedback(c, new LiteralText("§cgeneral.runtime_checks is disabled, you may need to restart the game for this change to take effect."), false);
			}
		}
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
