package com.unascribed.fabrication.mixin.c_tweaks.no_heavy_minecarts;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.vehicle.StorageMinecartEntity;

@Mixin(StorageMinecartEntity.class)
@EligibleIf(configEnabled="*.no_heavy_minecarts")
public abstract class MixinStorageMinecartEntity {

	@ModifyVariable(method="applySlowdown()V", at=@At(value="STORE", ordinal=1))
	private float undoComparatorModifier(float original) {
		if (MixinConfigPlugin.isEnabled("*.no_heavy_minecarts")) return 0.995f;
		return original;
	}
}