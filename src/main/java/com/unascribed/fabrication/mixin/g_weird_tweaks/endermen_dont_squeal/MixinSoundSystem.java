package com.unascribed.fabrication.mixin.g_weird_tweaks.endermen_dont_squeal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;

@Mixin(SoundSystem.class)
@EligibleIf(configEnabled="*.endermen_dont_squeal", envMatches=Env.CLIENT)
public class MixinSoundSystem {

	@Inject(at=@At("HEAD"), method="play(Lnet/minecraft/client/sound/SoundInstance;)V", cancellable=true)
	public void play(SoundInstance si, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.endermen_dont_squeal")) return;
		if (si != null && si.getId().getNamespace().equals("minecraft")) {
			if (si.getId().getPath().equals("entity.enderman.scream") || si.getId().getPath().equals("entity.enderman.stare")) {
				ci.cancel();
			}
		}
	}
	
}
