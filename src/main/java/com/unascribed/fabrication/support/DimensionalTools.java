package com.unascribed.fabrication.support;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.unascribed.fabrication.loaders.LoaderDimensionalTools;
import com.unascribed.fabrication.loaders.LoaderDimensionalTools.MohsIdentifier;
import com.unascribed.fabrication.loaders.LoaderDimensionalTools.NameSubstitution;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Language;

import java.text.NumberFormat;
import java.util.Set;

public class DimensionalTools {

	public static final NumberFormat format = NumberFormat.getNumberInstance();
	static {
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(2);
	}

	public static void handleCraft(CraftingInventory inv, ItemStack stack) {
		if (LoaderDimensionalTools.isSubstitutable(stack.getItem())) {
			int count = 0;
			Multiset<MohsIdentifier> dimensions = null;
			for (int i = 0; i < inv.size(); i++) {
				ItemStack ing = inv.getStack(i);
				if (!ing.isEmpty()) {
					Set<MohsIdentifier> assoc = LoaderDimensionalTools.getAssociatedDimensionsForIngredient(ing);
					if (assoc.isEmpty()) return;
					if (assoc.contains(null)) continue;
					count++;
					if (dimensions == null) dimensions = HashMultiset.create();
					dimensions.addAll(assoc);
				}
			}
			if (dimensions != null) {
				Set<MohsIdentifier> finalDimensions = Sets.newHashSet();
				for (Multiset.Entry<MohsIdentifier> en : dimensions.entrySet()) {
					if (en.getCount() == count) {
						finalDimensions.add(en.getElement());
					}
				}
				if (!finalDimensions.isEmpty()) {
					String s = Language.getInstance().get(stack.getTranslationKey());
					for (NameSubstitution sub : LoaderDimensionalTools.nameSubstitutions) {
						if (finalDimensions.contains(new MohsIdentifier(true, sub.dimId)) ||
								finalDimensions.contains(new MohsIdentifier(false, sub.dimId))) {
							s = s.replace(sub.find, sub.replace);
						}
					}
					stack.setCustomName(new LiteralText("§f"+s));
					NbtList li = new NbtList();
					for (MohsIdentifier dim : finalDimensions) {
						li.add(NbtString.of(dim.toString()));
					}
					stack.getNbt().put("fabrication:HonoraryDimensions", li);
				}
			}
		}
	}

}
