package com.unascribed.fabrication.mixin.d_minor_mechanics.feather_falling_five;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.feather_falling_five")
public class MixinLivingEntity {

	@FabInject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable=true)
	public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.feather_falling_five")) return;
		LivingEntity self = ((LivingEntity)(Object)this);
		ItemStack boots = self.getEquippedStack(EquipmentSlot.FEET);
		if (source == DamageSource.FALL && ConfigPredicates.shouldRun("*.feather_falling_five", self)) {
			if (FabConf.isEnabled("*.feather_falling_five_damages_boots") && ConfigPredicates.shouldRun("*.feather_falling_five_damages_boots", self) && !boots.isEmpty() && amount >= 2) {
				boots.damage((int)(amount/2), self, (e) -> {
					e.sendEquipmentBreakStatus(EquipmentSlot.FEET);
				});
			}
			cir.setReturnValue(false);
		}
	}

}
