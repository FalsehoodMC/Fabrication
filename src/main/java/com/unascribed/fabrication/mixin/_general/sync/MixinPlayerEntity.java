package com.unascribed.fabrication.mixin._general.sync;

import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity implements SetFabricationConfigAware {
	
	private boolean fabrication$configAware;

	@Override
	public void fabrication$setConfigAware(boolean aware) {
		fabrication$configAware = aware;
	}

	@Override
	public boolean fabrication$isConfigAware() {
		return fabrication$configAware;
	}
	
}
