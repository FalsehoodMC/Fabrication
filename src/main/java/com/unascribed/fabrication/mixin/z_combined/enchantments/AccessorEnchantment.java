package com.unascribed.fabrication.mixin.z_combined.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Enchantment.class)
public interface AccessorEnchantment {
	@Accessor("exclusiveSet")
	@Mutable
	void setExclusiveSet(RegistryEntryList<Enchantment> value);
}
