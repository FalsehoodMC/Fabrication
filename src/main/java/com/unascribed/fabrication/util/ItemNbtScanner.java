package com.unascribed.fabrication.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

//..item nbt removal did a number on this one
public class ItemNbtScanner {
	public static final TagKey<Item> INCLUSIONS = TagKey.of(RegistryKeys.ITEM, Identifier.of("fabrication", "include_in_item_inventory_check"));
	public static final TagKey<Item> EXCEPTIONS = TagKey.of(RegistryKeys.ITEM, Identifier.of("fabrication", "exclude_from_item_inventory_check"));
	public static boolean hasItemInvNBT(ItemStack stack) {
		if (stack.isIn(INCLUSIONS)) return true;
		if (stack.isIn(EXCEPTIONS)) return false;
		BundleContentsComponent component1 = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
		if (component1 != null && component1.stream().filter(is -> !is.isEmpty()).mapToInt(e -> 1).sum() > 0) return true;
		ContainerComponent component2 = stack.get(DataComponentTypes.CONTAINER);
		if (component2 != null && component2.stream().filter(is -> !is.isEmpty()).mapToInt(e -> 1).sum() > 0) return true;
		if (stack.contains(DataComponentTypes.CONTAINER_LOOT)) return true;
		return false;
	}
}
