package com.unascribed.fabrication.mixin.d_minor_mechanics.feather_falling_five;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.ProtectionEnchantment;

@Mixin(ProtectionEnchantment.class)
@EligibleIf(configAvailable="*.feather_falling_five")
public class MixinProtectionEnchantment {

	@FabInject(at=@At("RETURN"), method="getMaxLevel()I", cancellable=true)
	public void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
		if (!FabConf.isEnabled("*.feather_falling_five")) return;
		if (((Object)this) == Enchantments.FEATHER_FALLING && cir.getReturnValueI() < 5) {
			cir.setReturnValue(5);
		}
	}

}
