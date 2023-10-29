package com.unascribed.fabrication.mixin._general.sync;

import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;

import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity implements SetFabricationConfigAware {

	private int fabrication$configReqVer = -1;

	@Override
	public void fabrication$setReqVer(int reqVer) {
		fabrication$configReqVer = reqVer;
	}

	@Override
	public int fabrication$getReqVer() {
		return fabrication$configReqVer;
	}

}
