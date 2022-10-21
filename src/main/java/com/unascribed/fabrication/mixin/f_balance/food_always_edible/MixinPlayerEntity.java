package com.unascribed.fabrication.mixin.f_balance.food_always_edible;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.food_always_edible", modNotLoaded="eternaleats")
public class MixinPlayerEntity {

	private static final Predicate<PlayerEntity> fabrication$foodAlwaysEdiblePredicate = ConfigPredicates.getFinalPredicate("*.food_always_edible");
	@FabInject(method="canConsume(Z)Z", at=@At("HEAD"), cancellable=true)
	public void canConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.food_always_edible")) return;
		if (!fabrication$foodAlwaysEdiblePredicate.test((PlayerEntity) (Object) this)) return;
		cir.setReturnValue(true);
	}

}
