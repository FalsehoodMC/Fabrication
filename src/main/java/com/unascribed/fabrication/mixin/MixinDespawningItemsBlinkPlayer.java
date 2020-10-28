package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.SetItemDespawnAware;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
@EligibleIf(configEnabled="*.despawning_items_blink")
public class MixinDespawningItemsBlinkPlayer implements SetItemDespawnAware {
	
	private boolean fabrication$itemDespawnAware;

	@Override
	public void fabrication$setItemDespawnAware(boolean aware) {
		fabrication$itemDespawnAware = aware;
	}

	@Override
	public boolean fabrication$isItemDespawnAware() {
		return fabrication$itemDespawnAware;
	}
	
}
