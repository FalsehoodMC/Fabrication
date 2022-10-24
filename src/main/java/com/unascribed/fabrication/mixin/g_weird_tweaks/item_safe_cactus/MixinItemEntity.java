package com.unascribed.fabrication.mixin.g_weird_tweaks.item_safe_cactus;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;

@Mixin(ItemEntity.class)
@EligibleIf(configAvailable="*.item_safe_cactus")
public class MixinItemEntity {

	@FabInject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
	public void onEntityCollision(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.item_safe_cactus") && source.equals(DamageSource.CACTUS))
			cir.setReturnValue(false);
	}
}
