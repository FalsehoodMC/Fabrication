package com.unascribed.fabrication.mixin.f_balance.shield_stagger;

import com.google.common.collect.ImmutableList;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.shield_stagger")
public abstract class MixinLivingEntity {

	@Shadow
	protected ItemStack activeItemStack;
	private static final Predicate<List<?>> fabrication$shieldStaggerPredicate = ConfigPredicates.getFinalPredicate("*.shield_stagger");
	@FabInject(method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
			at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
	public void brittleShield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!(FabConf.isEnabled("*.shield_stagger") && fabrication$shieldStaggerPredicate.test(ImmutableList.of(this, source)))) return;
		Object self = this;
		if (!(self instanceof ServerPlayerEntity)) return;
		((ServerPlayerEntity)self).getItemCooldownManager().set(activeItemStack.getItem(), 120);
		((ServerPlayerEntity)self).stopUsingItem();
	}
}
