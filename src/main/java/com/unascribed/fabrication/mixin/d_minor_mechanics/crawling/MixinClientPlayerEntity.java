package com.unascribed.fabrication.mixin.d_minor_mechanics.crawling;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.SetCrawling;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
@EligibleIf(configAvailable="*.crawling", envMatches=Env.CLIENT)
public class MixinClientPlayerEntity {

	@Shadow
	private boolean inSneakingPose;

	@FabInject(method="tickMovement()V", at=@At("TAIL"))
	public void fixCrawlPose(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.crawling")) return;
		if (this instanceof SetCrawling && (((SetCrawling) this).fabrication$isCrawling())) {
			inSneakingPose = false;
		}
	}

}
