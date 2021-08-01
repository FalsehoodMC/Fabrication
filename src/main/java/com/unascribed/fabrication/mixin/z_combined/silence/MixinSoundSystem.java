package com.unascribed.fabrication.mixin.z_combined.silence;

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
@EligibleIf(anyConfigAvailable={"*.disable_equip_sound", "*.endermen_dont_squeal", "*.silent_minecarts"}, envMatches=Env.CLIENT)
public class MixinSoundSystem {

	@Inject(at=@At("HEAD"), method="play(Lnet/minecraft/client/sound/SoundInstance;)V", cancellable=true)
	public void play(SoundInstance si, CallbackInfo ci) {
		if (si != null && si.getId().getNamespace().equals("minecraft")) {
			if (MixinConfigPlugin.isEnabled("*.disable_equip_sound") && si.getId().getPath().equals("item.armor.equip_generic")) {
				ci.cancel();
			} else if (MixinConfigPlugin.isEnabled("*.endermen_dont_squeal") && (si.getId().getPath().equals("entity.enderman.scream") || si.getId().getPath().equals("entity.enderman.stare"))) {
				ci.cancel();
			} else if (MixinConfigPlugin.isEnabled("*.silent_minecarts") && (si.getId().getPath().equals("entity.minecart.inside") || si.getId().getPath().equals("entity.minecart.riding"))) {
				ci.cancel();
			} else if (MixinConfigPlugin.isEnabled("*.disable_bees") && (si.getId().getPath().startsWith("entity.bee.") || si.getId().getPath().startsWith("block.beehive."))) {
				ci.cancel();
			}
		}
	}
	
}
