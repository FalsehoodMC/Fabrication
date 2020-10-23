package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.ProtectionEnchantment;

@Mixin(ProtectionEnchantment.class)
@EligibleIf(configEnabled="*.feather_falling_five")
public class MixinFeatherFallingFiveEnchantment {

	@Inject(at=@At("RETURN"), method="getMaxLevel()I", cancellable=true)
	public void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
		if (!RuntimeChecks.check("*.feather_falling_five")) return;
		if (((Object)this) == Enchantments.FEATHER_FALLING && cir.getReturnValueI() < 5) {
			cir.setReturnValue(5);
		}
	}
	
}
