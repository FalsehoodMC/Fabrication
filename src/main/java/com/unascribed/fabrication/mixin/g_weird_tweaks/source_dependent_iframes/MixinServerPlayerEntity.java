package com.unascribed.fabrication.mixin.g_weird_tweaks.source_dependent_iframes;

import com.unascribed.fabrication.interfaces.TickSourceIFrames;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.source_dependent_iframes")
public abstract class MixinServerPlayerEntity {

	@Inject(at=@At("HEAD"), method= "tick()V")
	private void tickSourceDependentIFrames(CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.source_dependent_iframes")) return;
		((TickSourceIFrames)this).fabrication$tickSourceDependentIFrames();
	}
	
}
