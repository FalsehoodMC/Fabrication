package com.unascribed.fabrication.mixin.f_balance.disable_mending;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(ExperienceOrbEntity.class)
@EligibleIf(configAvailable="*.disable_mending")
public class MixinExperienceOrbEntity {

	@FabInject(method = "repairPlayerGears(Lnet/minecraft/entity/player/PlayerEntity;I)I", at=@At("HEAD"), cancellable = true)
	public void no_repair(PlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir) {
		if (FabConf.isEnabled("*.disable_mending") && ConfigPredicates.shouldRun("*.disable_mending", player)) cir.setReturnValue(amount);
	}

}
