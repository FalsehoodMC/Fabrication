package com.unascribed.fabrication.mixin.d_minor_mechanics.feather_falling_five;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.ProtectionEnchantment;

@Mixin(ProtectionEnchantment.class)
@EligibleIf(configEnabled="*.feather_falling_five")
public class MixinProtectionEnchantment {

	@Inject(at=@At("RETURN"), method="getMaxLevel()I", cancellable=true, expect=1)
	public void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
		if (!MixinConfigPlugin.isEnabled("*.feather_falling_five")) return;
		if (((Object)this) == Enchantments.FEATHER_FALLING && cir.getReturnValueI() < 5) {
			cir.setReturnValue(5);
		}
	}
	
}
