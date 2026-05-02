package com.unascribed.fabrication.mixin.g_weird_tweaks.source_dependent_iframes;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.TickSourceIFrames;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.source_dependent_iframes")
public abstract class MixinServerPlayerEntity {

	@FabInject(at=@At("HEAD"), method= "tick()V")
	private void tickSourceDependentIFrames(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.source_dependent_iframes")) return;
		((TickSourceIFrames)this).fabrication$tickSourceDependentIFrames();
	}

}
