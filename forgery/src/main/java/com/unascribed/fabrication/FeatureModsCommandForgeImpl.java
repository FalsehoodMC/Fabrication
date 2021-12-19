package com.unascribed.fabrication;

import java.util.List;
import java.util.Optional;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.support.Feature;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;

public class FeatureModsCommandForgeImpl implements Feature {

	private boolean registered = false;
	private boolean applied = false;
	
	@Override
	public void apply() {
		applied = true;
		if (!registered) {
			registered = true;
			Agnos.runForCommandRegistration((dispatcher, dedi) -> {
				dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("mods")
						.requires(s -> applied)
						.then(LiteralArgumentBuilder.<CommandSourceStack>literal("all")
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
					dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("plugins")
							.executes((c) -> {
								c.getSource().sendSuccess(new TextComponent("§cThis ain't no Bukkit!\nTry /mods"), false);
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

	private void sendMods(CommandContext<CommandSourceStack> c, boolean all) {
		try {
			MutableComponent mt = new TextComponent("Mods: ");
			boolean first = true;
			for (IModInfo mi : ModList.get().getMods()) {
				if (mi.getModId().equals("minecraft")) continue;
				if (!first) {
					mt.append(new TextComponent(", ").withStyle(Style.EMPTY.applyFormat(ChatFormatting.RESET)));
				} else {
					first = false;
				}
				StringBuilder desc = new StringBuilder(mi.getDescription().replace("\r", ""));
				if (desc.length() > 0) {
					desc.append("\n\n");
				}
				Optional<Object> authorsOpt = mi instanceof ModInfo ? ((ModInfo)mi).getConfigElement("authors") : Optional.empty();
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
				MutableComponent lt = new TextComponent(mi.getDisplayName());
				Style s = Style.EMPTY.applyFormat(ChatFormatting.GREEN)
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(desc.toString())));
				Optional<String> displayUrlOpt = mi instanceof ModInfo ? ((ModInfo)mi).getConfigElement("displayURL") : Optional.empty();
				if (displayUrlOpt.isPresent()) {
					s = s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, displayUrlOpt.get()));
				}
				lt.setStyle(s);
				mt.append(lt);
			}
			c.getSource().sendSuccess(mt, false);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public String getConfigKey() {
		return "*.mods_command";
	}

}
