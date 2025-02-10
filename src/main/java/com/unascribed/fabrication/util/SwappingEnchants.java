package com.unascribed.fabrication.util;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryArrayList;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryIdentifier;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryNbt;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryPair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public class SwappingEnchants {
	private static final Predicate<PlayerEntity> configPredicate = ConfigPredicates.getFinalPredicate("*.swap_conflicting_enchants");
	public static boolean swapEnchants(ItemStack self, World world, PlayerEntity user) {
		if (!FabConf.isEnabled("*.swap_conflicting_enchants")) return false;
		if (!configPredicate.test(user)) return false;
		List<Pair<String, Integer>> currentConflicts = ForgeryArrayList.get();
		NbtComponent component = self.get(DataComponentTypes.CUSTOM_DATA);
		if (component == null) return false;
		NbtCompound nbt = component.copyNbt();
		if (nbt == null) return false;

		NbtCompound lTag = nbt.getCompound("fabrication#conflictingEnchants");
		if (lTag == null || lTag.isEmpty()) return false;
		for (String key : lTag.getKeys()) {
			currentConflicts.add(ForgeryPair.get(key, lTag.getInt(key)));
		}
		if (!currentConflicts.isEmpty()) {
			NbtCompound tag = ForgeryNbt.getCompound();
			Pair<String, Integer> toAdd;
			{
				int rmi = world.random.nextInt(currentConflicts.size());
				toAdd = currentConflicts.get(rmi);
				currentConflicts.remove(rmi);
			}
			RegistryEntry<Enchantment> toAddEnchant = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(ForgeryIdentifier.get(toAdd.getLeft())).orElse(null);
			Object2IntOpenHashMap<RegistryEntry<Enchantment>> currentEnchantments = new Object2IntOpenHashMap<>();
			currentEnchantments.put(toAddEnchant, toAdd.getRight().intValue());
			if (self.hasEnchantments()) {
				for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : EnchantmentHelper.getEnchantments(self).getEnchantmentEntries()) {
					if (Enchantment.canBeCombined(entry.getKey(), toAddEnchant)) {
						currentEnchantments.put(entry.getKey(), entry.getIntValue());
					} else {
						tag.putInt(String.valueOf(world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getId(entry.getKey().value())), entry.getIntValue());
					}
				}
			}
			for (Pair<String, Integer> entry : currentConflicts) {
				RegistryEntry<Enchantment> enchant = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(ForgeryIdentifier.get(entry.getLeft())).orElse(null);
				if (currentEnchantments.keySet().stream().anyMatch(e->!Enchantment.canBeCombined(e, enchant))) {
					tag.putInt(entry.getLeft(), entry.getRight());
					continue;
				}
				currentEnchantments.put(enchant, entry.getRight().intValue());
			}
			ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
			for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : currentEnchantments.object2IntEntrySet()) {
				builder.add(entry.getKey(), entry.getIntValue());
			}
			EnchantmentHelper.set(self, builder.build());
			if (tag.isEmpty()) {
				nbt.remove("fabrication#conflictingEnchants");
			} else {
				nbt.put("fabrication#conflictingEnchants", tag);
			}
			self.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1, 1);
			return true;
		}
		return false;
	}
}
