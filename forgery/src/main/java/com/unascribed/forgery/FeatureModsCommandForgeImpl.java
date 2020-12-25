package com.unascribed.forgery;

import java.util.List;
import java.util.Optional;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.support.Feature;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

public class FeatureModsCommandForgeImpl implements Feature {

	private boolean registered = false;
	private boolean applied = false;
	
	@Override
	public void apply() {
		applied = true;
		if (!registered) {
			registered = true;
			Agnos.INST.runForCommandRegistration((dispatcher, dedi) -> {
				dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("mods")
						.requires(s -> applied)
						.then(LiteralArgumentBuilder.<CommandSource>literal("all")
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
					dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("plugins")
							.executes((c) -> {
								c.getSource().sendFeedback(new StringTextComponent("§cThis ain't no Bukkit!\nTry /mods"), false);
								return 1;
							}));
				}
			});
		}
	}
	
	@Override
	public boolean undo() {
		applied = false;
		return true;
	}

	private void sendMods(CommandContext<CommandSource> c, boolean all) {
		try {
			TextComponent mt = new StringTextComponent("Mods: ");
			boolean first = true;
			for (ModInfo mi : ModList.get().getMods()) {
				if (mi.getModId().equals("minecraft")) continue;
				if (!first) {
					mt.append(new StringTextComponent(", ").mergeStyle(Style.EMPTY.applyFormatting(TextFormatting.RESET)));
				} else {
					first = false;
				}
				StringBuilder desc = new StringBuilder(mi.getDescription().replace("\r", ""));
				if (desc.length() > 0) {
					desc.append("\n\n");
				}
				Optional<Object> authorsOpt = mi.getConfigElement("authors");
				if (authorsOpt.isPresent()) {
					desc.append("Authors: ");
					if (authorsOpt.get() instanceof List) {
						boolean firstAuthor = true;
						for (String s : (List<String>)authorsOpt.get()) {
							if (!firstAuthor) {
								desc.append(", ");
							} else {
								firstAuthor = false;
							}
							desc.append(s);
						}
					} else {
						desc.append(authorsOpt.get().toString());
					}
					desc.append("\n");
				}
				desc.append("ID: ");
				desc.append(mi.getModId());
				StringTextComponent lt = new StringTextComponent(mi.getDisplayName());
				Style s = Style.EMPTY.applyFormatting(TextFormatting.GREEN)
						.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(desc.toString())));
				Optional<String> displayUrlOpt = mi.getConfigElement("displayURL");
				if (displayUrlOpt.isPresent()) {
					s = s.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, displayUrlOpt.get()));
				}
				lt.setStyle(s);
				mt.append(lt);
			}
			c.getSource().sendFeedback(mt, false);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public String getConfigKey() {
		return "*.mods_command";
	}

}
