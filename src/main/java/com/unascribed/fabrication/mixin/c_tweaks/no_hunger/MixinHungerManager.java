package com.unascribed.fabrication.mixin.c_tweaks.no_hunger;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.SetSaturation;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(HungerManager.class)
@EligibleIf(configAvailable="*.no_hunger")
public abstract class MixinHungerManager implements SetSaturation {

	@Shadow
	private float saturationLevel;

	@FabInject(at=@At("HEAD"), method="update(Lnet/minecraft/entity/player/PlayerEntity;)V", cancellable=true)
	public void update(PlayerEntity pe, CallbackInfo ci) {
		if (FabConf.isEnabled("*.no_hunger")) {
			if (ConfigPredicates.shouldRun("*.no_hunger", pe) && !pe.hasStatusEffect(StatusEffects.HUNGER)) {
				ci.cancel();
			}
		}
	}

	@Override
	public void fabrication$setSaturation(float sat) {
		saturationLevel = sat;
	}

}
