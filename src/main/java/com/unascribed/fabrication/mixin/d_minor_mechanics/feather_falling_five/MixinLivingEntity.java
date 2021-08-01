package com.unascribed.fabrication.mixin.d_minor_mechanics.feather_falling_five;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.feather_falling_five")
public class MixinLivingEntity {

	@Inject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable=true)
	public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!MixinConfigPlugin.isEnabled("*.feather_falling_five")) return;
		LivingEntity self = ((LivingEntity)(Object)this);
		ItemStack boots = self.getEquippedStack(EquipmentSlot.FEET);
		if (source == DamageSource.FALL && EnchantmentHelper.getLevel(Enchantments.FEATHER_FALLING, boots) >= 5) {
			if (MixinConfigPlugin.isEnabled("*.feather_falling_five_damages_boots") && amount >= 2) {
				boots.damage((int)(amount/2), self, (e) -> {
					e.sendEquipmentBreakStatus(EquipmentSlot.FEET);
				});
			}
			cir.setReturnValue(false);
		}
	}
	
}
