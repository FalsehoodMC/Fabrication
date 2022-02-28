package com.unascribed.fabrication.mixin.d_minor_mechanics.crawling;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.SetCrawling;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.crawling")
public class MixinEntity {

	@Inject(at=@At("HEAD"), method="wouldPoseNotCollide(Lnet/minecraft/entity/EntityPose;)Z", cancellable=true)
	public void wouldPoseNotCollide(EntityPose pose, CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isEnabled("*.crawling") && this instanceof SetCrawling) {
			if (((SetCrawling)this).fabrication$isCrawling() && (pose == EntityPose.STANDING || pose == EntityPose.CROUCHING)) {
				// pretend we're in a small space
				// vanilla tries very hard to force you out of swimming pose whenever possible
				ci.setReturnValue(true);
			}
		}
	}

}
