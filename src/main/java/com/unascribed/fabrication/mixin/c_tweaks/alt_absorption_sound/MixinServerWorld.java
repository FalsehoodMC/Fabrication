package com.unascribed.fabrication.mixin.c_tweaks.alt_absorption_sound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.DidJustAbsorp;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
@EligibleIf(configEnabled="*.alt_absorption_sound")
public class MixinServerWorld {
	
	@Inject(at=@At("HEAD"), method="sendEntityStatus(Lnet/minecraft/entity/Entity;B)V", cancellable=true)
	public void sendEntityStatus(Entity entity, byte status, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.alt_absorption_sound")) {
			if (status == 2 || status == 33 || status == 36 || status == 37 || status == 44) {
				if (entity instanceof DidJustAbsorp && ((DidJustAbsorp)entity).fabrication$didJustAbsorp()) {
					ci.cancel();
				}
			}
		}
	}
	
}
