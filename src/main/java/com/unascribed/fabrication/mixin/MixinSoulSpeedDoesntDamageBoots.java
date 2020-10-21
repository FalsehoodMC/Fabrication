package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.unascribed.fabrication.support.OnlyIf;

import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntity.class)
@OnlyIf(config="tweaks.soul_speed_doesnt_damage_boots")
public class MixinSoulSpeedDoesntDamageBoots {

	@Inject(at=@At(value="INVOKE", target="net/minecraft/item/ItemStack.damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"), method="addSoulSpeedBoostIfNeeded()V", cancellable=true)
	protected void addSoulSpeedBoostIfNeeded(CallbackInfo ci) {
		ci.cancel();
	}
	
}
