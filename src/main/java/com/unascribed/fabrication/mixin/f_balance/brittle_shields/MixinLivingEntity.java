package com.unascribed.fabrication.mixin.f_balance.brittle_shields;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.brittle_shields")
public abstract class MixinLivingEntity {

	@Shadow
	protected ItemStack activeItemStack;

	@Shadow
	protected abstract void damageShield(float amount);

	@FabInject(method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
			at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
	public void brittleShield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!(FabConf.isEnabled("*.brittle_shields") && source.isIn(DamageTypeTags.IS_EXPLOSION))) return;
		damageShield(activeItemStack.getItem().getMaxDamage());
	}
}
