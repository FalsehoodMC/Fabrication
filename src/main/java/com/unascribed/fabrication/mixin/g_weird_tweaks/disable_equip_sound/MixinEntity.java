package com.unascribed.fabrication.mixin.g_weird_tweaks.disable_equip_sound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.disable_equip_sound")
public class MixinEntity {

	@Inject(at=@At("HEAD"), method="playSound(Lnet/minecraft/sound/SoundEvent;FF)V", cancellable=true)
	public void playSound(SoundEvent event, float f, float f2, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.disable_equip_sound")) return;
		if (event == SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
			ci.cancel();
		}
	}
	
}
