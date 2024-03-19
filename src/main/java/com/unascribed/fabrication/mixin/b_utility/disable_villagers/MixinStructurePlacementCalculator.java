package com.unascribed.fabrication.mixin.b_utility.disable_villagers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSetKeys;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(StructurePlacementCalculator.class)
@EligibleIf(configAvailable="*.disable_villagers")
public abstract class MixinStructurePlacementCalculator {
	@Unique
	private static boolean fabrication$testForVillages(RegistryEntry<StructureSet> structureSetRegistryEntry) {
		return structureSetRegistryEntry.matchesKey(StructureSetKeys.VILLAGES);
	}
	@ModifyReturn(method="create(Lnet/minecraft/world/gen/noise/NoiseConfig;JLnet/minecraft/world/biome/source/BiomeSource;Ljava/util/stream/Stream;)Lnet/minecraft/world/gen/chunk/placement/StructurePlacementCalculator;",
	target="Ljava/util/stream/Stream;toList()Ljava/util/List;")
	private static List<RegistryEntry<StructureSet>> fabrication$disableVillages1(List<RegistryEntry<StructureSet>> list) {
		if (!FabConf.isEnabled("*.disable_villagers")) return list;
		(list = new ArrayList<>(list)).removeIf(MixinStructurePlacementCalculator::fabrication$testForVillages);
		return list;
	}
	@ModifyReturn(method="create(Lnet/minecraft/world/gen/noise/NoiseConfig;JLnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/registry/RegistryWrapper;)Lnet/minecraft/world/gen/chunk/placement/StructurePlacementCalculator;",
		target="Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;")
	private static List<RegistryEntry<StructureSet>> fabrication$disableVillages2(List<RegistryEntry<StructureSet>> list) {
		return fabrication$disableVillages1(list);
	}
}
