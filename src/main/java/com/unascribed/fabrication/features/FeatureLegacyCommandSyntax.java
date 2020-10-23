package com.unascribed.fabrication.features;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import com.google.common.collect.Lists;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

@EligibleIf(configEnabled="*.legacy_command_syntax", modLoaded="fabric")
public class FeatureLegacyCommandSyntax implements Feature {

	private boolean applied = false;
	
	@Override
	public void apply() {
		if (applied) return;
		applied = true;
		CommandRegistrationCallback.EVENT.register((dispatcher, dedi) -> {
			dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("gamemode")
					.requires(s -> MixinConfigPlugin.isEnabled("*.legacy_command_syntax"))
					.then(LiteralArgumentBuilder.<ServerCommandSource>literal("0")
							.redirect(dispatcher.findNode(Lists.newArrayList("gamemode", "survival")))));
		});
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
