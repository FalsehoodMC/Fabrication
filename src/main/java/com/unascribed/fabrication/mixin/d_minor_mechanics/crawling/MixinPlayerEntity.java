package com.unascribed.fabrication.mixin.d_minor_mechanics.crawling;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.SetCrawling;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.crawling")
public class MixinPlayerEntity implements SetCrawling {

	private boolean fabrication$crawling;

	@ModifyReturn(method="updatePose()V", target="Lnet/minecraft/entity/player/PlayerEntity;isSwimming()Z")
	public boolean fabrication$updateSwimming(boolean old) {
		return old || !FabConf.isEnabled("*.crawling") ? old : fabrication$crawling;
	}

	@Override
	public void fabrication$setCrawling(boolean b) {
		fabrication$crawling = b;
	}

	@Override
	public boolean fabrication$isCrawling() {
		return fabrication$crawling;
	}

}
