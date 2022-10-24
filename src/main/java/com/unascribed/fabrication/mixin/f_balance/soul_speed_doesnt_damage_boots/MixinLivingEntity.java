package com.unascribed.fabrication.mixin.f_balance.soul_speed_doesnt_damage_boots;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.soul_speed_doesnt_damage_boots")
public class MixinLivingEntity {

	@FabInject(at=@At(value="INVOKE", target="net/minecraft/item/ItemStack.damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"), method="addSoulSpeedBoostIfNeeded()V", cancellable=true)
	protected void addSoulSpeedBoostIfNeeded(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.soul_speed_doesnt_damage_boots")) return;
		ci.cancel();
	}

}
