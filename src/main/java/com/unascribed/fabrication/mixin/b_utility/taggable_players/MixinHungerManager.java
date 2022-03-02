package com.unascribed.fabrication.mixin.b_utility.taggable_players;

import com.unascribed.fabrication.FabConf;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.SetSaturation;
import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.logic.PlayerTag;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(HungerManager.class)
@EligibleIf(configAvailable="*.taggable_players")
public abstract class MixinHungerManager implements SetSaturation {

	@Shadow
	private float foodSaturationLevel;

	@Inject(at=@At("HEAD"), method="update(Lnet/minecraft/entity/player/PlayerEntity;)V", cancellable=true)
	public void update(PlayerEntity pe, CallbackInfo ci) {
		if (FabConf.isEnabled("*.taggable_players") && pe instanceof TaggablePlayer) {
			if (((TaggablePlayer)pe).fabrication$hasTag(PlayerTag.NO_HUNGER) && !pe.hasStatusEffect(StatusEffects.HUNGER)) {
				ci.cancel();
			}
		}
	}

	@Override
	public void fabrication$setSaturation(float sat) {
		foodSaturationLevel = sat;
	}

}
