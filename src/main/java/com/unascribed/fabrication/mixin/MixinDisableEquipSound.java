package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.unascribed.fabrication.support.OnlyIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

@Mixin(Entity.class)
@OnlyIf(config="tweaks.disable_equip_sound")
public class MixinDisableEquipSound {

	@Inject(at=@At("HEAD"), method="playSound(Lnet/minecraft/sound/SoundEvent;FF)V", cancellable=true)
	public void playSound(SoundEvent event, float f, float f2, CallbackInfo ci) {
		if (!RuntimeChecks.check("tweaks.disable_equip_sound")) return;
		if (event == SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
			ci.cancel();
		}
	}
	
}
