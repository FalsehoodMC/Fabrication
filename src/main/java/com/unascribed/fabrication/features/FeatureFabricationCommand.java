package com.unascribed.fabrication.features;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabricationClientCommands;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.FeaturesFile.Sides;
import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.loaders.LoaderFScript;
import com.unascribed.fabrication.logic.PlayerTag;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.MixinConfigPlugin.Profile;
import com.unascribed.fabrication.support.OptionalFScript;
import com.unascribed.fabrication.util.Cardinal;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;

public class FeatureFabricationCommand implements Feature {

	@Override
	public void apply() {
		Agnos.runForCommandRegistration((dispatcher, dedi) -> {
			try {
				LiteralArgumentBuilder<ServerCommandSource> root = LiteralArgumentBuilder.<ServerCommandSource>literal("fabrication");
				addConfig(root, dedi);
				if (Agnos.isModLoaded("fscript")) addFScript(root, dedi);

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

				LiteralArgumentBuilder<ServerCommandSource> analyze = LiteralArgumentBuilder.<ServerCommandSource>literal("analyze");
				analyze.requires(scs -> scs.hasPermissionLevel(4));
				{
					LiteralArgumentBuilder<ServerCommandSource> biome = CommandManager.literal("biome");


					for (Map.Entry<RegistryKey<Biome>, Biome> en : BuiltinRegistries.BIOME.getEntries()) {
						Identifier id = en.getKey().getValue();
						Identifier b = en.getKey().getValue();
						Command<ServerCommandSource> exec = c -> {
							Set<Identifier> set = Sets.newHashSet(b);
							World w;
							try {
								c.getArgument("dimension", Identifier.class);
								w = DimensionArgumentType.getDimensionArgument(c, "dimension");
							} catch (IllegalArgumentException e) {
								w = c.getSource().getEntityOrThrow().world;
							}
							return analyzeBlockDistribution(c, w, set);
						};
						if (id.getNamespace().equals("minecraft")) {
							biome.then(CommandManager.literal(id.getPath())
									.executes(exec));
						}
						biome.then(CommandManager.literal(id.toString())
								.executes(exec));
					}

					analyze.then(CommandManager.literal("block_distribution")
							.executes(c -> analyzeBlockDistribution(c, c.getSource().getEntityOrThrow().world, null))
							.then(biome)
							.then(CommandManager.literal("in")
									.then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
											.then(biome)
											.executes(c -> analyzeBlockDistribution(c, DimensionArgumentType.getDimensionArgument(c, "dimension"), null)))));
				}
				root.then(analyze);

				dispatcher.register(root);
			} catch (Throwable t) {
				FabricationMod.featureError(this, t);
			}
		});
	}

	private int analyzeBlockDistribution(CommandContext<ServerCommandSource> c, World world, Set<Identifier> biomesIn) {
		Set<Biome> biomes;
		if (biomesIn != null) {
			biomes = Sets.newHashSet();
			for (Identifier b : biomesIn) {
				biomes.add(c.getSource().getServer().getRegistryManager().get(Registry.BIOME_KEY).get(b));
			}
		} else {
			biomes = null;
		}
		String name = "fabrication_block_distribution_"+System.currentTimeMillis()+".tsv";
		c.getSource().sendFeedback(new LiteralText("Starting background block distribution analysis"), false);
		c.getSource().sendFeedback(new LiteralText("This could take a while, but the server should remain usable"), false);
		c.getSource().sendFeedback(new LiteralText("Once complete a file named "+name+" will appear in the server directory"), false);
		c.getSource().sendFeedback(new LiteralText("Progress reports will go to the console"), false);
		new Thread((Runnable)() -> {
			int x = 0;
			int z = 0;
			Cardinal dir = Cardinal.WEST;
			int legLength = 0;
			int i = 0;
			int j = 0;

			int scannedChunks = 0;

			long scanned = 0;
			long skipped = 0;
			long goal = (biomes == null ? 8000 : 1000)*16*16*world.getHeight();

			class MutableLong { long value = 1; }

			Map<BlockState, MutableLong> counts = Maps.newHashMap();

			// scan in a counterclockwise outward spiral from 0, 0
			out: while (true) {
				Chunk chunk = world.getChunk(x, z, ChunkStatus.FULL, false);
				if (chunk == null) {
					try {
						final int fx = x;
						final int fz = z;
						chunk = world.getServer().submit(() -> world.getChunk(fx, fz, ChunkStatus.FULL, true)).get();
					} catch (Exception e) {
						FabLog.warn("Failed to generate chunk at "+x+", "+z+" for block distribution analysis");
					}
				}
				if (chunk != null) {
					synchronized (chunk) {
						for (int cY = 0; cY < chunk.getHeight(); cY++) {
							for (int cX = 0; cX < 16; cX++) {
								for (int cZ = 0; cZ < 16; cZ++) {
									if (biomes != null) {
										Biome b = ((ServerWorld)world).getChunkManager().getChunkGenerator().getBiomeSource().getBiomeForNoiseGen(cX+chunk.getPos().getStartX(), cY, cZ+chunk.getPos().getStartZ());
										if (!biomes.contains(b)) {
											skipped++;
											if (skipped > goal && scanned == 0) {
												FabLog.warn("We have skipped more blocks than our goal and found nothing matching the given biome. Giving up.");
												return;
											}
											continue;
										}
									}
									ChunkSection section = chunk.getSectionArray()[cY/16];
									BlockState state;
									if (section != null) {
										state = section.getBlockState(cX, cY%16, cZ);
									} else {
										state = Blocks.VOID_AIR.getDefaultState();
									}
									counts.compute(state, (bs, ml) -> {
										if (ml == null) return new MutableLong();
										ml.value++;
										return ml;
									});
									scanned++;
									if (scanned >= goal) break out;
								}
							}
						}
					}
					scannedChunks++;
					if (scannedChunks%20 == 0) {
						FabLog.info("Scanned "+scanned+"/"+goal+" blocks... (skipped "+skipped+") "+((scanned*100)/goal)+"% done");
					}
				}
				if (i >= legLength) {
					dir = dir.ccw();
					i = 0;
					j++;
					if (j % 2 == 0) {
						legLength++;
					}
				}
				x += dir.xOfs();
				z += dir.yOfs();
				i++;
			}
			FabLog.info("Scanned "+scanned+"/"+goal+" blocks (skipped "+skipped+"), 100% done. Writing file");
			FabLog.info("NOTE: Fabrication block distribution analysis is NOT A BENCHMARK. Chunk generation speed is intentionally limited to keep servers responsive and not crashing.");
			List<Map.Entry<BlockState, MutableLong>> sorted = Lists.newArrayList(counts.entrySet());
			sorted.sort((a, b) -> Long.compare(b.getValue().value, a.getValue().value));
			try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(new File(name)), Charsets.UTF_8)) {
				osw.write("blockstate\tpercentage\r\n");
				BigDecimal scannedBD = new BigDecimal(scanned);
				BigDecimal hundred = new BigDecimal(100);
				for (Map.Entry<BlockState, MutableLong> en : sorted) {
					osw.write(Registry.BLOCK.getId(en.getKey().getBlock()).toString().replace("\t", "    "));
					if (!en.getKey().getEntries().isEmpty()) {
						osw.write("[");
						boolean first = true;
						for (Map.Entry<Property<?>, Comparable<?>> stateEn : en.getKey().getEntries().entrySet()) {
							if (first) {
								first = false;
							} else {
								osw.write(",");
							}
							osw.write(stateEn.getKey().getName().replace("\t", "    "));
							osw.write("=");
							osw.write(((Property)stateEn.getKey()).name(stateEn.getValue()).replace("\t", "    "));
						}
						osw.write("]");
					}
					osw.write("\t");
					osw.write(new BigDecimal(en.getValue().value).divide(scannedBD, MathContext.DECIMAL64).multiply(hundred).toString());
					osw.write("\r\n");
				}
				FabLog.info(name+" written to disk.");
			} catch (IOException e) {
				FabLog.error("Failed to save block distribution data", e);
			}
		}, "Fabrication block analysis").start();
		return 1;
	}

	public static <T extends CommandSource> void addConfig(LiteralArgumentBuilder<T> root, boolean dediServer) {
		LiteralArgumentBuilder<T> config = LiteralArgumentBuilder.<T>literal("config");
		config.requires(s -> {
			// always allow a client to reconfigure itself
			if (!(s instanceof ServerCommandSource)) return true;

			ServerCommandSource scs = (ServerCommandSource)s;
			if (scs.hasPermissionLevel(2)) return true;
			if (scs.getServer().isSinglePlayer() && scs.getEntity() != null) {
				Entity e = scs.getEntity();
				if (e instanceof PlayerEntity) {
					if (scs.getServer().getUserName().equals(((PlayerEntity)e).getGameProfile().getName())) {
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
					boolean tri = MixinConfigPlugin.isStandardValue(s);
					if (value.isEmpty() && tri) value = "unset";
					boolean def = MixinConfigPlugin.getDefault(s);
					LiteralText txt = new LiteralText(s+" = "+value+(tri ? " (default "+def+")" : ""));
					if (tri && !MixinConfigPlugin.isEnabled(s)) {
						// so that command blocks report failure
						throw new CommandException(txt.formatted(Formatting.WHITE));
					} else {
						sendFeedback(c, txt, false);
					}
					return 1;
				});
				get.then(key);
				setAltKeys(s, alt -> get.then(LiteralArgumentBuilder.<T>literal(alt).executes(key.getCommand())));
			}
			config.then(get);
			LiteralArgumentBuilder<T> set = LiteralArgumentBuilder.<T>literal("set");
			for (String s : MixinConfigPlugin.getAllKeys()) {
				if (dediServer && FeaturesFile.get(s).sides == Sides.CLIENT_ONLY) continue;
				LiteralArgumentBuilder<T> key = LiteralArgumentBuilder.<T>literal(s);

				String[] values;
				if (s.equals("general.reduced_motion") || s.equals("general.data_upload")) {
					values = new String[]{"true", "false"};
				} else if (s.equals("general.profile")) {
					values = Profile.stringValues();
				} else {
					values = new String[]{"unset", "true", "false", "banned"};
				}
				for (String v : values) {
					LiteralArgumentBuilder<T> value =
							LiteralArgumentBuilder.<T>literal(v)
							.executes((c) -> {
								setKeyWithFeedback(c, s, v);
								return 1;
							});
					key.then(value);
				}
				set.then(key);
				setAltKeys(s, alt -> {
					LiteralArgumentBuilder<T> short_key = LiteralArgumentBuilder.<T>literal(alt);
					for (CommandNode<T> arg : key.getArguments())
						short_key.then(arg);
					set.then(short_key);
				});
			}
			config.then(set);
			config.then(LiteralArgumentBuilder.<T>literal("reload")
					.executes((c) -> {
						MixinConfigPlugin.reload();
						if (c.getSource() instanceof ServerCommandSource) {
							FabricationMod.sendConfigUpdate(((ServerCommandSource)c.getSource()).getServer(), null);
						}
						sendFeedback(c, new LiteralText("Fabrication configuration reloaded"), true);
						sendFeedback(c, new LiteralText("§eYou may need to restart the game for the changes to take effect."), false);
						return 1;
					})
					);
		}
		root.then(config);
	}

	public static <T extends CommandSource> void addFScript(LiteralArgumentBuilder<T> root, boolean dediServer) {
		LiteralArgumentBuilder<T> script = LiteralArgumentBuilder.<T>literal("fscript");
		script.requires(s -> s.hasPermissionLevel(2));
		{
			LiteralArgumentBuilder<T> get = LiteralArgumentBuilder.<T>literal("get");
			for (String s : OptionalFScript.predicateProviders.keySet()) {
				if (dediServer && FeaturesFile.get(s).sides == FeaturesFile.Sides.CLIENT_ONLY) continue;
				LiteralArgumentBuilder<T> key = LiteralArgumentBuilder.<T>literal(s).executes((c) -> {
					sendFeedback(c, new LiteralText(s+ ": "+ LoaderFScript.get(s)), false);
					return 1;
				});
				get.then(key);
				setAltKeys(s, alt -> get.then(LiteralArgumentBuilder.<T>literal(alt).executes(key.getCommand())));
			}
			script.then(get);
			LiteralArgumentBuilder<T> set = LiteralArgumentBuilder.<T>literal("set");
			for (String s : OptionalFScript.predicateProviders.keySet()) {
				if (dediServer && FeaturesFile.get(s).sides == FeaturesFile.Sides.CLIENT_ONLY) continue;
				LiteralArgumentBuilder<T> key = LiteralArgumentBuilder.<T>literal(s);
				RequiredArgumentBuilder<T, String> value =
						RequiredArgumentBuilder.<T, String>argument("script", StringArgumentType.string())
						.executes((c) -> {
							OptionalFScript.set(c, s, c.getArgument("script", String.class));
							return 1;
						});
				key.then(value);
				set.then(key);
				setAltKeys(s, alt -> set.then(LiteralArgumentBuilder.<T>literal(alt).then(value)));
			}
			script.then(set);

			LiteralArgumentBuilder<T> unset = LiteralArgumentBuilder.<T>literal("unset");
			for (String s : OptionalFScript.predicateProviders.keySet()) {
				if (dediServer && FeaturesFile.get(s).sides == FeaturesFile.Sides.CLIENT_ONLY) continue;
				LiteralArgumentBuilder<T> key = LiteralArgumentBuilder.<T>literal(s).executes((c) -> {
					OptionalFScript.restoreDefault(s);
					sendFeedback(c, new LiteralText("Restored default behaviour for "+s), true);
					return 1;
				});
				unset.then(key);
				setAltKeys(s, alt -> unset.then(LiteralArgumentBuilder.<T>literal(alt).executes(key.getCommand())));
			}
			script.then(unset);
			script.then(LiteralArgumentBuilder.<T>literal("reload")
					.executes((c) -> {
						LoaderFScript.reload();
						OptionalFScript.reload();
						sendFeedback(c, new LiteralText("Fabrication fscript reloaded"), true);
						return 1;
					})
					);
		}
		root.then(script);
	}

	public static void sendFeedback(CommandContext<? extends CommandSource> c, LiteralText text, boolean broadcast) {
		if (c.getSource() instanceof ServerCommandSource) {
			((ServerCommandSource)c.getSource()).sendFeedback(text, broadcast);
		} else {
			sendFeedbackClient(c, text);
		}
	}

	private static void sendFeedbackClient(CommandContext<? extends CommandSource> c, LiteralText text) {
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
		boolean tri = MixinConfigPlugin.isStandardValue(key);
		if (value.equals(oldValue)) {
			sendFeedback(c, new LiteralText(key+" is already set to "+value+(tri ? " (default "+def+")" : "")), false);
		} else {
			MixinConfigPlugin.set(key, value);
			if (c.getSource() instanceof ServerCommandSource) {
				FabricationMod.sendConfigUpdate(((ServerCommandSource)c.getSource()).getServer(), key);
			}
			sendFeedback(c, new LiteralText(key+" is now set to "+value+(tri ? " (default "+def+")" : "")), true);
			if (FabricationMod.isAvailableFeature(key)) {
				if (FabricationMod.updateFeature(key)) {
					return;
				}
			}
		}
	}

	public static void setAltKeys(String key, Consumer<String> set){
		if(!key.contains(".")) return;
		for (int i = key.indexOf('.'); i != -1; i = key.indexOf('.', i+1))
			set.accept("*"+key.substring(i));
		if (key.lastIndexOf('.') != key.indexOf('.'))
			set.accept(key.substring(0,key.indexOf('.'))+key.substring(key.lastIndexOf('.')));
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
