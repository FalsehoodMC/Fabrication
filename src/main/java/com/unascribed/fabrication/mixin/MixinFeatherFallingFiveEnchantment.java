package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.OnlyIf;

import net.minecraft.enchantment.ProtectionEnchantment;

@Mixin(ProtectionEnchantment.class)
@OnlyIf(config="tweaks.feather_falling_five")
public class MixinFeatherFallingFiveEnchantment {

	@Inject(at=@At("RETURN"), method="getMaxLevel()I", cancellable=true)
	public void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
		if (cir.getReturnValueI() < 5) {
			cir.setReturnValue(5);
		}
	}
	
}
