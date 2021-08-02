package com.unascribed.fabrication.mixin.d_minor_mechanics.crawling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.SetCrawling;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
@EligibleIf(configEnabled="*.crawling")
public class MixinPlayerEntity implements SetCrawling {
	
	private boolean fabrication$crawling;
	
	@Inject(at=@At("HEAD"), method="jump()V", cancellable=true)
	public void jump(CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.crawling") && fabrication$crawling) {
			ci.cancel();
		}
	}
	
	@Inject(at=@At("HEAD"), method="updateSwimming()V", cancellable=true)
	public void updateSwimming(CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.crawling") && fabrication$crawling) {
			((PlayerEntity)(Object)this).setSwimming(true);
			ci.cancel();
		}
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
