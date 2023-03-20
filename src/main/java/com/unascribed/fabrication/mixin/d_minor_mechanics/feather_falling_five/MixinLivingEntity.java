package com.unascribed.fabrication.mixin.d_minor_mechanics.feather_falling_five;

import com.unascribed.fabrication.FabConf;
import net.minecraft.registry.tag.DamageTypeTags;
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

import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.feather_falling_five")
public class MixinLivingEntity {

	private static final Predicate<LivingEntity> fabrication$featherFallingPredicate = ConfigPredicates.getFinalPredicate("*.feather_falling_five");
	private static final Predicate<LivingEntity> fabrication$featherFallingBootsPredicate = ConfigPredicates.getFinalPredicate("*.feather_falling_five_damages_boots");
	@FabInject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable=true)
	public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.feather_falling_five")) return;
		LivingEntity self = ((LivingEntity)(Object)this);
		ItemStack boots = self.getEquippedStack(EquipmentSlot.FEET);
		if (source.isIn(DamageTypeTags.IS_FALL) && fabrication$featherFallingPredicate.test(self)) {
			if (FabConf.isEnabled("*.feather_falling_five_damages_boots") && fabrication$featherFallingBootsPredicate.test(self) && !boots.isEmpty() && amount >= 2) {
				boots.damage((int)(amount/2), self, (e) -> {
					e.sendEquipmentBreakStatus(EquipmentSlot.FEET);
				});
			}
			cir.setReturnValue(false);
		}
	}

}
