package com.unascribed.fabrication.loaders;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;

import com.unascribed.fabrication.Resolvable;
import com.unascribed.fabrication.support.ConfigLoader;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

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

		public ItemMaterialValue(double valueInIngots, String materialName, boolean ignoreDropRate) {
			this.valueInIngots = valueInIngots;
			this.materialName = materialName;
			this.ignoreDropRate = ignoreDropRate;
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
	public void load(Map<String, String> config) {
		dropRate = () -> 0.75;
		guaranteedIngots = 1;
		ignoreVanishing = true;
		cheat = 1;
		materials.clear();
		items.clear();
		double dropRateMin = 0.75;
		double dropRateMid = 0.75;
		double dropRateMax = 0.75;
		boolean dropRateUniform = false;
		for (Map.Entry<String, String> en : config.entrySet()) {
			String v = en.getValue();
			if (en.getKey().startsWith("@options.")) {
				String id = en.getKey().substring(9);
				if ("drop_rate_min".equals(id)) {
					dropRateMin = Double.parseDouble(v)/100;
				} else if ("drop_rate_mid".equals(id)) {
					dropRateMid = Double.parseDouble(v)/100;
				} else if ("drop_rate_max".equals(id)) {
					dropRateMax = Double.parseDouble(v)/100;
				} else if ("drop_rate_uniform".equals(id)) {
					dropRateUniform = Boolean.parseBoolean(v);
				} else if ("guaranteed_ingots".equals(id)) {
					if ("*".equals(v)) {
						guaranteedIngots = Integer.MAX_VALUE;
					} else {
						guaranteedIngots = Integer.parseInt(v);
					}
				} else if ("ignore_vanishing".equals(id)) {
					ignoreVanishing = Boolean.parseBoolean(v);
				} else if ("cheat".equals(id)) {
					cheat = Integer.parseInt(v);
				} else {
					throw new IllegalArgumentException("Unknown option key "+en.getKey());
				}
			} else if (en.getKey().startsWith("@materials.")) {
				String id = en.getKey().substring(11);
				String namespace = id.contains(":") ? id.substring(0, id.indexOf(':')) : "minecraft";
				String name = id.substring(id.indexOf(':')+1);
				List<String> split = SPACE_SPLITTER.splitToList(v);
				if (split.size() < 2) {
					throw new IllegalArgumentException("Bad value for "+en.getKey()+" - not enough values: "+en.getValue());
				}
				if (split.size() > 3) {
					throw new IllegalArgumentException("Bad value for "+en.getKey()+" - too many values: "+en.getValue());
				}
				int npi = Integer.parseInt(split.get(0));
				String nuggetId = split.get(1);
				String ingotId = split.size() >= 3 ? split.get(2) : null;
				Supplier<Item> nuggetGetter = resolver(namespace, nuggetId);
				Supplier<Item> ingotGetter = resolver(namespace, ingotId);
				materials.put(name, new MaterialData(npi, nuggetGetter, ingotGetter));
			} else {
				Resolvable<Item> r = Resolvable.of(new Identifier(en.getKey()), Registry.ITEM);
				for (String s : SPACE_SPLITTER.split(v)) {
					Matcher m = MATERIAL_VALUE_PATTERN.matcher(s);
					if (m.matches()) {
						double amt = Double.parseDouble(m.group(1));
						String mat = m.group(2);
						boolean idr = mat.endsWith("!");
						if (idr) {
							mat = mat.substring(0, mat.length()-1);
						}
						items.put(r, new ItemMaterialValue(amt, mat, idr));
					} else {
						throw new IllegalArgumentException("Syntax error for "+en.getKey()+" around "+s);
					}
				}
			}
		}
		for (Map.Entry<Resolvable<Item>, ItemMaterialValue> en : items.entries()) {
			if (!materials.containsKey(en.getValue().materialName)) {
				LogManager.getLogger("Fabrication").warn("Unknown material name "+en.getValue().materialName+" in drops for item "+en.getKey().getId());
			}
		}
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
	}

	private static Supplier<Item> resolver(String namespace, String id) {
		if (id == null) return () -> null;
		if (id.startsWith("#")) {
			return tagResolver(new Identifier(namespace, id.substring(1)));
		} else {
			return Resolvable.of(new Identifier(namespace, id), Registry.ITEM)::getOrNull;
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
