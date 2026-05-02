package com.unascribed.fabrication.mixin.c_tweaks.no_heavy_minecarts;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StorageMinecartEntity.class)
@EligibleIf(configAvailable="*.no_heavy_minecarts")
public abstract class MixinStorageMinecartEntity {

	@FabModifyVariable(method="applySlowdown()V", at=@At(value="STORE", ordinal=1))
	private float undoComparatorModifier(float original) {
		if (FabConf.isEnabled("*.no_heavy_minecarts")) return 0.995f;
		return original;
	}
}
