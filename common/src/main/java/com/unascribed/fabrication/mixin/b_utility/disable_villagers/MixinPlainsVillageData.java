package com.unascribed.fabrication.mixin.b_utility.disable_villagers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.structure.PlainsVillageData;
import net.minecraft.structure.pool.StructurePool;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlainsVillageData.class)
@EligibleIf(configAvailable="*.disable_villagers")
public class MixinPlainsVillageData {

	@Hijack(method="<clinit>()V", target="Lnet/minecraft/structure/pool/StructurePools;register(Lnet/minecraft/structure/pool/StructurePool;)Lnet/minecraft/util/registry/RegistryEntry;")
	private static HijackReturn fabrication$disableVillages(StructurePool templatePool) {
		if (!FabConf.isEnabled("*.disable_villagers") || PlainsVillageData.STRUCTURE_POOLS == null) return null;
		return HijackReturn.NULL;
	}

}
