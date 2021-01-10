package com.unascribed.fabrication.loaders;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.Resolvable;
import com.unascribed.fabrication.support.ConfigLoader;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LoaderGearComponents implements ConfigLoader {
	
	public static final class MaterialData {
		public final int nuggetsPerIngot;
		public final Supplier<Item> nuggetGetter;
		public final Supplier<Item> ingotGetter;
		
		public MaterialData(int nuggetsPerIngot, Supplier<Item> nuggetGetter, Supplier<Item> ingotGetter) {
			this.nuggetsPerIngot = nuggetsPerIngot;
			this.nuggetGetter = nuggetGetter;
			this.ingotGetter = ingotGetter;
		}
	}
	
	public static final class ItemMaterialValue {
		public final double valueInIngots;
		public final String materialName;
		public final boolean ignoreDropRate;
		public final boolean enchant;

		public ItemMaterialValue(double valueInIngots, String materialName, boolean ignoreDropRate, boolean enchant) {
			this.valueInIngots = valueInIngots;
			this.materialName = materialName;
			this.ignoreDropRate = ignoreDropRate;
			this.enchant = enchant;
		}
	}
	
	public static DoubleSupplier dropRate = () -> 0.75;
	
	public static int guaranteedIngots = 1;
	public static boolean ignoreVanishing = true;
	public static int cheat = 1;

	public static final Map<String, MaterialData> materials = Maps.newHashMap();
	public static final Multimap<Resolvable<Item>, ItemMaterialValue> items = ArrayListMultimap.create();
	
	private static final Random rand = new Random();
	
	private static final Splitter SPACE_SPLITTER = Splitter.on(' ');
	private static final Pattern MATERIAL_VALUE_PATTERN = Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)(.*)$");
	
	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		dropRate = () -> 0.75;
		materials.clear();
		items.clear();
		double dropRateMin = config.getDouble("@options.drop_rate_min").orElse(75D)/100;
		double dropRateMid = config.getDouble("@options.drop_rate_mid").orElse(75D)/100;
		double dropRateMax = config.getDouble("@options.drop_rate_max").orElse(75D)/100;
		boolean dropRateUniform = config.getBoolean("@options.drop_rate_uniform").orElse(false);
		
		if (dropRateMin == dropRateMid && dropRateMid == dropRateMax) {
			final double rate = dropRateMid;
			dropRate = () -> rate;
		} else if (dropRateUniform) {
			final double min = dropRateMin;
			final double max = dropRateMax;
			dropRate = () -> min+(rand.nextDouble()*(max-min));
		} else {
			final double min = dropRateMin;
			final double mid = dropRateMid;
			final double max = dropRateMax;
			dropRate = () -> {
				double value = rand.nextDouble()-rand.nextDouble();
				if (value < 0) {
					return mid+(value*(mid-min));
				} else if (value > 0) {
					return mid+(value*(max-mid));
				}
				return 0;
			};
		}
		
		Optional<String> guaranteedIngotsStr = config.get("@options.guaranteed_ingots");
		if (guaranteedIngotsStr.isPresent()) {
			if ("*".equals(guaranteedIngotsStr.get())) {
				guaranteedIngots = Integer.MAX_VALUE;
			} else {
				Integer i = Ints.tryParse(guaranteedIngotsStr.get());
				if (i == null) {
					FabLog.warn("@options.guaranteed_ingots must be * or a whole number (got "+guaranteedIngotsStr.get()+") at "+config.getBlame("@options.guaranteed_ingots"));
					guaranteedIngots = 1;
				} else {
					guaranteedIngots = i;
				}
			}
		} else {
			guaranteedIngots = 1;
		}
		ignoreVanishing = config.getBoolean("@options.ignore_vanishing").orElse(false);
		cheat = config.getInt("@options.cheat").orElse(1);
		for (String k : config.keySet()) {
			String v = config.get(k).get();
			if (k.startsWith("@materials.")) {
				String id = k.substring(11);
				String namespace = id.contains(":") ? id.substring(0, id.indexOf(':')) : "minecraft";
				String name = id.substring(id.indexOf(':')+1);
				List<String> split = SPACE_SPLITTER.splitToList(v);
				if (split.size() < 2) {
					FabLog.warn(k+" is not a valid material definition (got "+v+" which only has "+split.size()+" elements, need 2 or 3) at "+config.getBlame(k));
					continue;
				}
				if (split.size() > 3) {
					FabLog.warn(k+" is not a valid material definition (got "+v+" which has "+split.size()+" elements, need 2 or 3) at "+config.getBlame(k));
					continue;
				}
				int npi = Integer.parseInt(split.get(0));
				String nuggetId = split.get(1);
				String ingotId = split.size() >= 3 ? split.get(2) : null;
				Supplier<Item> nuggetGetter = resolver(namespace, nuggetId);
				Supplier<Item> ingotGetter = resolver(namespace, ingotId);
				materials.put(name, new MaterialData(npi, nuggetGetter, ingotGetter));
			} else if (!k.startsWith("@")) {
				Resolvable<Item> r = Resolvable.of(new Identifier(k), Registry.ITEM);
				for (String s : SPACE_SPLITTER.split(v)) {
					Matcher m = MATERIAL_VALUE_PATTERN.matcher(s);
					if (m.matches()) {
						double amt = Double.parseDouble(m.group(1));
						String mat = m.group(2);
						boolean ench = false;
						boolean idr = false;
						for (int i = 0; i < 2; i++) {
							if (mat.endsWith("!")) {
								idr = true;
								mat = mat.substring(0, mat.length()-1);
							}
							if (mat.endsWith("*")) {
								ench = true;
								mat = mat.substring(0, mat.length()-1);
							}
						}
						items.put(r, new ItemMaterialValue(amt, mat, idr, ench));
					} else {
						FabLog.warn(k+" is not a valid material value definition (got "+v+") at "+config.getBlame(k));
					}
				}
			}
		}
		for (Map.Entry<Resolvable<Item>, ItemMaterialValue> en : items.entries()) {
			if (!materials.containsKey(en.getValue().materialName)) {
				FabLog.warn("Unknown material name "+en.getValue().materialName+" in drops for item "+en.getKey().getId());
			}
		}
	}

	private static Supplier<Item> resolver(String namespace, String id) {
		if (id == null) return () -> null;
		if (id.startsWith("#")) {
			return tagResolver(FabricationMod.createIdWithCustomDefault(namespace, id.substring(1)));
		} else {
			return Resolvable.of(FabricationMod.createIdWithCustomDefault(namespace, id), Registry.ITEM)::getOrNull;
		}
	}

	private static Supplier<Item> tagResolver(Identifier id) {
		return () -> {
			Tag<Item> itemTag = ItemTags.getTagGroup().getTag(id);
			if (itemTag != null) return itemTag.getRandom(rand);
			Tag<Block> blockTag = BlockTags.getTagGroup().getTag(id);
			if (blockTag != null) return blockTag.getRandom(rand).asItem();
			return null;
		};
	}

	@Override
	public String getConfigName() {
		return "gear_components";
	}

}
