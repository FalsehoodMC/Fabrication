package com.unascribed.fabrication.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;

// to be used with "*.tools_in_bundles"
public class BundleHelper {
	public static final Fraction ONE_EIGHTH = Fraction.getFraction(1, 8);

	public static boolean isCompatible(ItemStack bundle, ItemStack stack) {
		boolean isTool = stack.getMaxCount() == 1;
		if (isTool && ItemNbtScanner.hasItemInvNBT(stack)) {
			return false;
		}
		BundleContentsComponent component = bundle.get(DataComponentTypes.BUNDLE_CONTENTS);
		if (component == null) return false;
		if (component.isEmpty()) return true;
		boolean containsTool = component.stream().anyMatch(is -> is.getMaxCount() == 1);
		return containsTool == isTool;
	}

	public static boolean isCompatible(List<ItemStack> stacks, ItemStack stack) {
		boolean isTool = stack.getMaxCount() == 1;
		if (isTool && ItemNbtScanner.hasItemInvNBT(stack)) {
			return false;
		}
		if (stacks.isEmpty() || stacks.stream().filter(is -> !is.isEmpty()).mapToInt(e -> 1).sum() == 0) return true;
		boolean containsTool = stacks.stream().anyMatch(is -> is.getMaxCount() == 1);
		return containsTool == isTool;
	}
}
