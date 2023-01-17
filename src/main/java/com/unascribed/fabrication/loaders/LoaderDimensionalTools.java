package com.unascribed.fabrication.loaders;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.support.ConfigLoader;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class LoaderDimensionalTools implements ConfigLoader {

	public static final class MohsIdentifier {
		private final boolean hard;
		private final Identifier id;

		public MohsIdentifier(boolean hard, Identifier id) {
			this.hard = hard;
			this.id = id;
		}

		public static MohsIdentifier createHard(Identifier id) {
			return new MohsIdentifier(true, id);
		}

		public static MohsIdentifier createSoft(Identifier id) {
			return new MohsIdentifier(false, id);
		}

		public static MohsIdentifier parse(String s) {
			boolean hard = s.endsWith("!");
			if (hard) s = s.substring(0, s.length()-1);
			return new MohsIdentifier(hard, new Identifier(s));
		}

		public MohsIdentifier asSoft() {
			if (!hard) return this;
			return new MohsIdentifier(false, id);
		}

		public boolean isHard() {
			return hard;
		}

		public Identifier getId() {
			return id;
		}

		public String getPath() {
			return id.getPath();
		}

		public String getNamespace() {
			return id.getNamespace();
		}

		@Override
		public String toString() {
			return id.toString()+(hard ? "!" : "");
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, hard);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			MohsIdentifier that = (MohsIdentifier) obj;
			if (this.hard != that.hard) return false;
			if (!Objects.equals(this.id, that.id)) return false;
			return true;
		}

	}

	public static final class NameSubstitution {
		public final Identifier dimId;
		public final String find;
		public final String replace;

		private NameSubstitution(Identifier dimId, String find, String replace) {
			this.dimId = dimId;
			this.find = find;
			this.replace = replace;
		}
	}

	@Retention(RetentionPolicy.SOURCE) @Documented @Target(ElementType.TYPE_USE)
	private @interface BlockId {}
	@Retention(RetentionPolicy.SOURCE) @Documented @Target(ElementType.TYPE_USE)
	private @interface BlockTagId {}
	@Retention(RetentionPolicy.SOURCE) @Documented @Target(ElementType.TYPE_USE)
	private @interface ItemId {}
	@Retention(RetentionPolicy.SOURCE) @Documented @Target(ElementType.TYPE_USE)
	private @interface ItemTagId {}
	@Retention(RetentionPolicy.SOURCE) @Documented @Target(ElementType.TYPE_USE)
	private @interface DimensionId {}

	public static final SetMultimap<@BlockId Identifier, @DimensionId MohsIdentifier> blockAssociations = HashMultimap.create();
	public static final SetMultimap<@BlockTagId Identifier, @DimensionId MohsIdentifier> blockTagAssociations = HashMultimap.create();

	public static final SetMultimap<@ItemId Identifier, @DimensionId MohsIdentifier> toolAssociations = HashMultimap.create();
	public static final SetMultimap<@ItemTagId Identifier, @DimensionId MohsIdentifier> toolTagAssociations = HashMultimap.create();

	public static final SetMultimap<@ItemId Identifier, @Nullable MohsIdentifier> ingredientAssociations = HashMultimap.create();
	public static final SetMultimap<@ItemTagId Identifier, @Nullable MohsIdentifier> ingredientTagAssociations = HashMultimap.create();

	public static final Set<@ItemId Identifier> substitutableItems = Sets.newHashSet();
	public static final Set<@ItemTagId Identifier> substitutableItemTags = Sets.newHashSet();

	public static final List<NameSubstitution> nameSubstitutions = Lists.newArrayList();

	// A>B
	public static final Table<@DimensionId MohsIdentifier, @DimensionId Identifier, Integer> oneToOneDamageFactors = HashBasedTable.create();
	// *>A
	public static final Map<@DimensionId Identifier, Integer> foreignOnNativeDamageFactors = Maps.newHashMap();
	// *!>A
	public static final Map<@DimensionId Identifier, Integer> foreignHardOnNativeDamageFactors = Maps.newHashMap();
	// A>*
	public static final Map<@DimensionId MohsIdentifier, Integer> nativeOnForeignDamageFactors = Maps.newHashMap();

	public static int getDamageFactor(@DimensionId @Nullable MohsIdentifier tool, @DimensionId @Nullable Identifier block) {
		if (tool != null) {
			if (oneToOneDamageFactors.contains(tool, block))
				return oneToOneDamageFactors.get(tool, block);
			MohsIdentifier toolSoft = tool.asSoft();
			if (tool.isHard() && oneToOneDamageFactors.contains(toolSoft, block))
				return oneToOneDamageFactors.get(toolSoft, block);
			if (!tool.getId().equals(block)) {
				if (nativeOnForeignDamageFactors.containsKey(tool))
					return nativeOnForeignDamageFactors.get(tool);
				if (tool.isHard() && nativeOnForeignDamageFactors.containsKey(toolSoft))
					return nativeOnForeignDamageFactors.get(toolSoft);
			}
		}
		if (tool == null || !tool.getId().equals(block)) {
			if (tool != null && tool.isHard() && foreignHardOnNativeDamageFactors.containsKey(block))
				return foreignHardOnNativeDamageFactors.get(block);
			if (foreignOnNativeDamageFactors.containsKey(block))
				return foreignOnNativeDamageFactors.get(block);
		}
		return 1;
	}

	public static Set<MohsIdentifier> getAssociatedDimensionsForTool(ItemStack stack) {
		Set<MohsIdentifier> dims = processTags(toolAssociations.get(Registries.ITEM.getId(stack.getItem())), toolTagAssociations, Registries.ITEM.getKey(), stack.getItem().getRegistryEntry());
		if (stack.hasNbt()) {
			if (stack.getNbt().getBoolean("fabrication:ActLikeGold")) {
				dims = Sets.newHashSet(dims);
				dims.add(new MohsIdentifier(true, DimensionTypes.THE_NETHER_ID));
			}
			if (stack.getNbt().contains("fabrication:HonoraryDimensions", NbtType.LIST)) {
				dims = Sets.newHashSet(dims);
				NbtList li = stack.getNbt().getList("fabrication:HonoraryDimensions", NbtType.STRING);
				for (int i = 0; i < li.size(); i++) {
					try {
						dims.add(MohsIdentifier.parse(li.getString(i)));
					} catch (InvalidIdentifierException e) {
						FabLog.warn("Bad honorary dimension "+li.getString(i)+" in stack "+Registries.ITEM.getId(stack.getItem())+stack.getNbt());
					}
				}
			}
		}
		return dims;
	}

	public static Set<MohsIdentifier> getAssociatedDimensionsForIngredient(ItemStack stack) {
		return processTags(ingredientAssociations.get(Registries.ITEM.getId(stack.getItem())), ingredientTagAssociations, Registries.ITEM.getKey(), stack.getItem().getRegistryEntry());
	}

	public static Set<MohsIdentifier> getAssociatedDimensions(Block block) {
		return processTags(blockAssociations.get(Registries.BLOCK.getId(block)), blockTagAssociations, Registries.BLOCK.getKey(), block.getRegistryEntry());
	}

	private static <T, U> Set<U> processTags(Set<U> out, SetMultimap<Identifier, U> assoc, RegistryKey<? extends Registry<T>> key, RegistryEntry<T> entry) {
		boolean cloned = false;
		for (Map.Entry<Identifier, U> en : assoc.entries()) {
			TagKey<T> tag = TagKey.of(key, en.getKey());
			if (entry.isIn(tag)) {
				if (!cloned) {
					out = Sets.newHashSet(out);
					cloned = true;
				}
				out.add(en.getValue());
			}
		}
		return out;
	}

	public static boolean isSubstitutable(Item item) {
		if (substitutableItems.contains(Registries.ITEM.getId(item))) return true;
		for (Identifier id : substitutableItemTags) {
			TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), id);
			if (item.getRegistryEntry().isIn(tag)) return true;
		}
		return false;
	}

	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		blockAssociations.clear();
		blockTagAssociations.clear();
		toolAssociations.clear();
		toolTagAssociations.clear();
		ingredientAssociations.clear();
		ingredientTagAssociations.clear();
		substitutableItems.clear();
		substitutableItemTags.clear();
		nameSubstitutions.clear();
		oneToOneDamageFactors.clear();
		foreignOnNativeDamageFactors.clear();
		foreignHardOnNativeDamageFactors.clear();
		nativeOnForeignDamageFactors.clear();
		for (String key : config.keySet()) {
			String realKey = key;
			String defaultNamespace = "minecraft";
			int colon = key.indexOf(':');
			if (colon != -1) {
				int dot = key.indexOf('.');
				defaultNamespace = key.substring(dot+1, colon);
				key = key.substring(0, dot+1)+key.substring(colon+1);
			}
			SetMultimap<Identifier, @DimensionId MohsIdentifier> targetMohs, tagTargetMohs;
			SetMultimap<Identifier, @DimensionId Identifier> target, tagTarget;
			Set<Identifier> setTarget, setTagTarget;
			boolean starValid = false;
			targetMohs = tagTargetMohs = null;
			target = tagTarget = null;
			setTarget = setTagTarget = null;
			if (key.startsWith("blocks.")) {
				key = key.substring(7);
				targetMohs = blockAssociations;
				tagTargetMohs = blockTagAssociations;
			} else if (key.startsWith("tools.")) {
				key = key.substring(6);
				targetMohs = toolAssociations;
				tagTargetMohs = toolTagAssociations;
			} else if (key.startsWith("materials.")) {
				key = key.substring(10);
				targetMohs = ingredientAssociations;
				tagTargetMohs = ingredientTagAssociations;
				starValid = true;
			} else if (key.startsWith("substitutable.")) {
				key = key.substring(14);
				setTarget = substitutableItems;
				setTagTarget = substitutableItemTags;
			} else if (key.startsWith("name_substitutions.")) {
				key = key.substring(19);
				int slash = key.indexOf('/');
				if (slash == -1) {
					FabLog.warn(key+" is not a valid substitution at "+config.getBlame(realKey, 0));
					continue;
				}
				String dim = key.substring(0, slash);
				String find = key.substring(slash+1);
				Identifier dimId;
				try {
					dimId = FabricationMod.createIdWithCustomDefault(defaultNamespace, dim);
				} catch (InvalidIdentifierException e) {
					FabLog.warn(key+" is not a valid identifier at "+config.getBlame(realKey, 0));
					continue;
				}
				for (String replace : config.getAll(realKey)) {
					nameSubstitutions.add(new NameSubstitution(dimId, find, replace));
				}
				continue;
			} else if (key.startsWith("damage_factors.")) {
				key = key.substring(15);
				int angle = key.indexOf('>');
				if (angle == -1) {
					FabLog.warn(key+" is not a valid damage factor mapping at "+config.getBlame(realKey));
					continue;
				}
				String tool = key.substring(0, angle);
				boolean toolHard = tool.endsWith("!");
				if (toolHard) tool = tool.substring(0, tool.length()-1);
				Identifier toolId = "*".equals(tool) ? null : FabricationMod.createIdWithCustomDefault(defaultNamespace, tool);
				String dim = key.substring(angle+1);
				Identifier dimId = "*".equals(dim) ? null : FabricationMod.createIdWithCustomDefault(defaultNamespace, dim);
				if (toolId == null && dimId == null) {
					FabLog.warn(key+" is not a valid damage factor mapping at "+config.getBlame(realKey));
					continue;
				}
				int value;
				Optional<Integer> valueOpt = config.getInt(realKey);
				if (!valueOpt.isPresent()) {
					if ("Infinity".equals(config.get(realKey))) {
						value = Integer.MAX_VALUE;
					} else {
						FabLog.warn(config.get(realKey)+" is not a valid damage factor value at "+config.getBlame(realKey));
						continue;
					}
				} else {
					value = valueOpt.get();
				}
				if (toolId == null) {
					(toolHard ? foreignHardOnNativeDamageFactors : foreignOnNativeDamageFactors).put(dimId, value);
				} else if (dimId == null) {
					nativeOnForeignDamageFactors.put(new MohsIdentifier(toolHard, toolId), value);
				} else {
					oneToOneDamageFactors.put(new MohsIdentifier(toolHard, toolId), dimId, value);
				}
				continue;
			}
			if (targetMohs != null || target != null || setTarget != null) {
				boolean tag = key.startsWith("#");
				if (tag) key = key.substring(1);
				Identifier keyId;
				try {
					keyId = FabricationMod.createIdWithCustomDefault(defaultNamespace, key);
				} catch (InvalidIdentifierException e) {
					FabLog.warn(key+" is not a valid identifier at "+config.getBlame(realKey, 0));
					continue;
				}
				if (targetMohs != null) {
					int i = 0;
					for (String s : config.getAll(realKey)) {
						boolean hard = s.endsWith("!");
						if (hard && targetMohs == null) {
							FabLog.warn(s+" is not a valid identifier at "+config.getBlame(realKey, i));
							continue;
						}
						if (hard) s = s.substring(0, s.length()-1);
						SetMultimap tgt;
						Function<Identifier, Object> wrapper;
						if (targetMohs != null) {
							wrapper = id -> new MohsIdentifier(hard, id);
						} else {
							wrapper = id -> id;
						}
						if (tag) {
							tgt = targetMohs != null ? tagTargetMohs : tagTarget;
						} else {
							tgt = targetMohs != null ? targetMohs : tagTarget;
						}
						if (s.equals("*")) {
							if (!starValid) {
								FabLog.warn(s+" is not a valid identifier at "+config.getBlame(realKey, i));
								continue;
							}
							tgt.put(keyId, null);
							continue;
						}
						try {
							Identifier valueId = FabricationMod.createIdWithCustomDefault(defaultNamespace, s);
							tgt.put(keyId, wrapper.apply(valueId));
						} catch (InvalidIdentifierException e) {
							FabLog.warn(s+" is not a valid identifier at "+config.getBlame(realKey, i));
							continue;
						}
						i++;
					}
				} else if (setTarget != null) {
					if (config.getBoolean(realKey).orElse(false)) {
						(tag ? setTagTarget : setTarget).add(keyId);
					}
				}
			} else {
				for (int i = 0; i < config.getAll(realKey).size(); i++) {
					FabLog.warn("Don't know what to do with "+config.getBlame(realKey, i));
				}
			}
		}
	}

	@Override
	public String getConfigName() {
		return "dimensional_tools";
	}

}
