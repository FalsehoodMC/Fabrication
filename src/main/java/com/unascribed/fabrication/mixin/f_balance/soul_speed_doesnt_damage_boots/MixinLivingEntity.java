package com.unascribed.fabrication.mixin.f_balance.soul_speed_doesnt_damage_boots;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntity.class)
@EligibleIf(configEnabled="*.soul_speed_doesnt_damage_boots")
public class MixinLivingEntity {

	@Inject(at=@At(value="INVOKE", target="net/minecraft/item/ItemStack.damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"), method="addSoulSpeedBoostIfNeeded()V", cancellable=true)
	protected void addSoulSpeedBoostIfNeeded(CallbackInfo ci) {
		if (!RuntimeChecks.check("*.soul_speed_doesnt_damage_boots")) return;
		ci.cancel();
	}
	
}
