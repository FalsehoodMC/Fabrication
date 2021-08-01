package com.unascribed.fabrication.mixin.g_weird_tweaks.item_safe_cactus;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
@EligibleIf(configAvailable="*.item_safe_cactus")
public class MixinItemEntity {

	@Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("HEAD"), cancellable = true)
	public void onEntityCollision(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (MixinConfigPlugin.isEnabled("*.item_safe_cactus") && source.equals(DamageSource.CACTUS))
			cir.setReturnValue(false);
	}
}
