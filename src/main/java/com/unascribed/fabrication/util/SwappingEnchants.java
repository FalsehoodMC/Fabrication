package com.unascribed.fabrication.util;

import com.unascribed.fabrication.util.forgery_nonsense.ForgeryArrayList;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryHashMap;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryIdentifier;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryNbt;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryPair;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

public class SwappingEnchants {
	public static boolean swapEnchants(ItemStack self, World world, PlayerEntity user) {
		List<Pair<String, Integer>> currentConflicts = ForgeryArrayList.get();
		NbtCompound nbt = self.getNbt();
		if (nbt == null) return false;

		NbtCompound lTag = nbt.getCompound("fabrication#conflictingEnchants");
		if (lTag == null || lTag.isEmpty()) return false;
		for (String key : lTag.getKeys()) {
			currentConflicts.add(ForgeryPair.get(key, lTag.getInt(key)));
		}
		if (!currentConflicts.isEmpty() && user.isSneaky()) {
			NbtCompound tag = ForgeryNbt.getCompound();
			Pair<String, Integer> toAdd;
			{
				int rmi = world.random.nextInt(currentConflicts.size());
				toAdd = currentConflicts.get(rmi);
				currentConflicts.remove(rmi);
			}
			Enchantment toAddEnchant = Registry.ENCHANTMENT.get(ForgeryIdentifier.get(toAdd.getLeft()));
			Map<Enchantment, Integer> currentEnchantments = ForgeryHashMap.get();
			currentEnchantments.put(toAddEnchant, toAdd.getRight());
			if (self.hasEnchantments()) {
				for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get(self).entrySet()) {
					if (entry.getKey().canCombine(toAddEnchant)) {
						currentEnchantments.put(entry.getKey(), entry.getValue());
					} else {
						tag.putInt(String.valueOf(Registry.ENCHANTMENT.getId(entry.getKey())), entry.getValue());
					}
				}
			}
			for (Pair<String, Integer> entry : currentConflicts) {
				Enchantment enchant = Registry.ENCHANTMENT.get(ForgeryIdentifier.get(entry.getLeft()));
				if (currentEnchantments.keySet().stream().anyMatch(e->!e.canCombine(enchant))) {
					tag.putInt(entry.getLeft(), entry.getRight());
					continue;
				}
				currentEnchantments.put(enchant, entry.getRight());
			}
			EnchantmentHelper.set(currentEnchantments, self);
			if (tag.isEmpty()) {
				nbt.remove("fabrication#conflictingEnchants");
			} else {
				nbt.put("fabrication#conflictingEnchants", tag);
			}

			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1, 1);
			return true;
		}
		return false;
	}
}
