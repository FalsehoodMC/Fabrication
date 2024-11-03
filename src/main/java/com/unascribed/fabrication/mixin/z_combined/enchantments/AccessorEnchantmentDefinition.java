package com.unascribed.fabrication.mixin.z_combined.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(Enchantment.Definition.class)
public interface AccessorEnchantmentDefinition {
	@Accessor("supportedItems")
	@Mutable
	void setSupportedItems(RegistryEntryList<Item> value);

	@Accessor("primaryItems")
	@Mutable
	void setPrimaryItems(Optional<RegistryEntryList<Item>> value);

	@Accessor("maxLevel")
	@Mutable
	void setMaxLevel(int value);
}
