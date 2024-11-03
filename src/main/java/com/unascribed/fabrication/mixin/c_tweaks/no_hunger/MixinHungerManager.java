package com.unascribed.fabrication.mixin.c_tweaks.no_hunger;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.NoHungerAdd;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(value=HungerManager.class, priority=200)
@EligibleIf(configAvailable="*.no_hunger")
public abstract class MixinHungerManager implements NoHungerAdd {
	@Unique
	private float fabrication$noHungerHeal = 0f;

	@Shadow
	private float saturationLevel;

	@Shadow
	private int foodLevel;
	private static final Predicate<PlayerEntity> fabrication$noHungerPredicate = ConfigPredicates.getFinalPredicate("*.no_hunger");

	@FabInject(at=@At("HEAD"), method="eat(Lnet/minecraft/component/type/FoodComponent;)V", cancellable=true)
	public void eatFood(FoodComponent foodComponent, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.no_hunger")) return;
		if (foodComponent == null) return;
		setFabrication$noHungerHeal(foodComponent.nutrition(), foodComponent.saturation());
	}

	@Unique
	public void setFabrication$noHungerHeal(int food, float sat) {
		fabrication$noHungerHeal += (sat = (food+sat)*0.75f) < 1f ? sat*.5f : (int) sat;
	}

	@FabInject(at=@At("HEAD"), method="update(Lnet/minecraft/entity/player/PlayerEntity;)V", cancellable=true)
	public void update(PlayerEntity pe, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.no_hunger")) return;
		if (!fabrication$noHungerPredicate.test(pe)) {
			fabrication$noHungerHeal = 0f;
			return;
		}
		if (fabrication$noHungerHeal != 0f) {
			pe.heal(fabrication$noHungerHeal);
			fabrication$noHungerHeal = 0f;
		}
		this.foodLevel = pe.hasStatusEffect(StatusEffects.HUNGER) ? 0 : pe.getHealth() >= pe.getMaxHealth() ? 20 : 17;
		// prevent the hunger bar from jiggling
		this.saturationLevel = 10;
		if (!pe.hasStatusEffect(StatusEffects.HUNGER)) ci.cancel();
	}
}
