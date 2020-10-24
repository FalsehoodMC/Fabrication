package com.unascribed.fabrication.features;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.Profile;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

@EligibleIf(modLoaded="fabric")
public class FeatureFabricationCommand implements Feature {
	
	@Override
	public void apply() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedi) -> {
			LiteralArgumentBuilder<ServerCommandSource> root = LiteralArgumentBuilder.<ServerCommandSource>literal("fabrication");
			root.requires(scs -> {
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
			LiteralArgumentBuilder<ServerCommandSource> config = LiteralArgumentBuilder.<ServerCommandSource>literal("config");
			LiteralArgumentBuilder<ServerCommandSource> get = LiteralArgumentBuilder.<ServerCommandSource>literal("get");
			for (String s : MixinConfigPlugin.getAllKeys()) {
				LiteralArgumentBuilder<ServerCommandSource> key = LiteralArgumentBuilder.<ServerCommandSource>literal(s);
				key.executes((c) -> {
					String value = MixinConfigPlugin.getRawValue(s);
					boolean tri = MixinConfigPlugin.isTrilean(s);
					if (value.isEmpty() && tri) value = "unset";
					boolean def = MixinConfigPlugin.getDefault(s);
					c.getSource().sendFeedback(new LiteralText(s+" = "+value+(tri ? " (default "+def+")" : "")), false);
					return 1;
				});
				get.then(key);
			}
			config.then(get);
			LiteralArgumentBuilder<ServerCommandSource> set = LiteralArgumentBuilder.<ServerCommandSource>literal("set");
			for (String s : MixinConfigPlugin.getAllKeys()) {
				LiteralArgumentBuilder<ServerCommandSource> key = LiteralArgumentBuilder.<ServerCommandSource>literal(s);
				String[] values;
				if (s.equals("general.runtime_checks")) {
					values = new String[]{"true", "false"};
				} else if (s.equals("general.profile")) {
					values = Profile.stringValues();
				} else {
					values = new String[]{"unset", "true", "false"};
				}
				for (String v : values) {
					key.then(LiteralArgumentBuilder.<ServerCommandSource>literal(v)
							.executes((c) -> {
								setKeyWithFeedback(c, s, v);
								return 1;
							}));
				}
				set.then(key);
			}
			config.then(set);
			config.then(LiteralArgumentBuilder.<ServerCommandSource>literal("reload")
				.executes((c) -> {
					MixinConfigPlugin.reload();
					c.getSource().sendFeedback(new LiteralText("fabrication.ini reloaded"), true);
					c.getSource().sendFeedback(new LiteralText("§eYou may need to restart the game for the changes to take effect."), false);
					return 1;
				})
			);
			root.then(config);
			dispatcher.register(root);
		});
	}

	private void setKeyWithFeedback(CommandContext<ServerCommandSource> c, String key, String value) {
		String oldValue = MixinConfigPlugin.getRawValue(key);
		boolean def = MixinConfigPlugin.getDefault(key);
		boolean tri = MixinConfigPlugin.isTrilean(key);
		if (value.equals(oldValue)) {
			c.getSource().sendFeedback(new LiteralText(key+" is already set to "+value+(tri ? " (default "+def+")" : "")), false);
		} else {
			MixinConfigPlugin.set(key, value);
			c.getSource().sendFeedback(new LiteralText(key+" is now set to "+value+(tri ? " (default "+def+")" : "")), true);
			if (FabricationMod.isAvailableFeature(key)) {
				if (FabricationMod.updateFeature(key)) {
					return;
				}
			}
			if ("general.runtime_checks".equals(key)) {
				c.getSource().sendFeedback(new LiteralText("§cYou will need to restart the game for this change to take effect."), false);
			} else if (!RuntimeChecks.ENABLED && !MixinConfigPlugin.isRuntimeConfigurable(key)) {
				c.getSource().sendFeedback(new LiteralText("§cgeneral.runtime_checks is disabled, you may need to restart the game for this change to take effect."), false);
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
