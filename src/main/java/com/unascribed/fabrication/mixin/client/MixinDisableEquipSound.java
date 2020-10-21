package com.unascribed.fabrication.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.OnlyIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;

@Mixin(SoundSystem.class)
@OnlyIf(config="tweaks.disable_equip_sound", env=Env.CLIENT)
public class MixinDisableEquipSound {

	@Inject(at=@At("HEAD"), method="play(Lnet/minecraft/client/sound/SoundInstance;)V", cancellable=true)
	public void play(SoundInstance si, CallbackInfo ci) {
		if (!RuntimeChecks.check("tweaks.disable_equip_sound")) return;
		if (si != null && si.getId().getNamespace().equals("minecraft")) {
			if (si.getId().getPath().equals("item.armor.equip_generic")) {
				ci.cancel();
			}
		}
	}
	
}
