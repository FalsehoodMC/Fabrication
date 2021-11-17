package com.unascribed.fabrication.features;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.SpecialEligibility;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

@EligibleIf(configEnabled="*.mods_command", specialConditions=SpecialEligibility.NOT_FORGE)
public class FeatureModsCommandFabric implements Feature {

	private boolean applied = false;

	@Override
	public void apply() {
		if (applied) return;
		applied = true;
		CommandRegistrationCallback.EVENT.register((dispatcher, dedi) -> {
			try {
				dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("mods")
						.requires(s -> MixinConfigPlugin.isEnabled("*.mods_command"))
						.then(LiteralArgumentBuilder.<ServerCommandSource>literal("all")
								.executes((c) -> {
									sendMods(c, true);
									return 1;
								}))
						.executes((c) -> {
							sendMods(c, false);
							return 1;
						}));
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
			} catch (Throwable t) {
				FabricationMod.featureError(this, t);
			}
		});
	}

	@Override
	public boolean undo() {
		return true;
	}

	private void sendMods(CommandContext<ServerCommandSource> c, boolean all) {
		MutableText mt = new LiteralText("Mods: ");
		boolean first = true;
		for (ModContainer mc : FabricLoader.getInstance().getAllMods()) {
			ModMetadata mm = mc.getMetadata();
			if (mm.getId().equals("minecraft")) continue;
			if (mm.getId().startsWith("fabric-") && !all) continue;
			if (!first) {
				mt.append(new LiteralText(", ").setStyle(Style.EMPTY.withColor(Formatting.RESET)));
			} else {
				first = false;
			}
			StringBuilder desc = new StringBuilder(mm.getDescription().replace("\r", ""));
			if (desc.length() > 0) {
				desc.append("\n\n");
			}
			if (!mm.getAuthors().isEmpty()) {
				desc.append("Authors: ");
				boolean firstAuthor = true;
				for (Person p : mm.getAuthors()) {
					if (!firstAuthor) {
						desc.append(", ");
					} else {
						firstAuthor = false;
					}
					desc.append(p.getName());
				}
				desc.append("\n");
			}
			desc.append("ID: ");
			desc.append(mm.getId());
			LiteralText lt = new LiteralText(mm.getName());
			Style s = Style.EMPTY.withColor(Formatting.GREEN)
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(desc.toString())));
			if (mm.getContact() != null && mm.getContact().get("homepage").isPresent()) {
				s = s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, mm.getContact().get("homepage").get()));
			}
			lt.setStyle(s);
			mt.append(lt);
		}
		c.getSource().sendFeedback(mt, false);
	}

	@Override
	public String getConfigKey() {
		return "*.mods_command";
	}

}
