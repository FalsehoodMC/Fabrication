package com.unascribed.fabrication.mixin.c_tweaks.no_heavy_minecarts;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyVariable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.vehicle.StorageMinecartEntity;

@Mixin(StorageMinecartEntity.class)
@EligibleIf(configAvailable="*.no_heavy_minecarts")
public abstract class MixinStorageMinecartEntity {

	@FabModifyVariable(method="applySlowdown()V", at=@At(value="STORE", ordinal=1))
	private float undoComparatorModifier(float original) {
		if (FabConf.isEnabled("*.no_heavy_minecarts")) return 0.995f;
		return original;
	}
}
