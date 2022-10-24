package com.unascribed.fabrication.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemNbtScanner {
	public static boolean hasItemInvNBT(ItemStack stack) {
		return check(stack.getTag());
	}
	private static boolean check(NbtElement tag) {
		if (tag == null) return false;
		if (tag instanceof NbtString) {
			Identifier id = Identifier.tryParse(tag.asString());
			return id != null && Registry.ITEM.containsId(id);
		}
		if (tag instanceof NbtCompound) {
			for (String key : ((NbtCompound) tag).getKeys()) {
				if (check(((NbtCompound) tag).get(key))) return true;
			}
		}
		return false;
	}
}
