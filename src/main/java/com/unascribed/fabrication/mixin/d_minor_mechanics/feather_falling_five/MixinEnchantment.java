package com.unascribed.fabrication.mixin.d_minor_mechanics.feather_falling_five;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.weapons_accept_silk")
public abstract class MixinEnchantment {

	@FabInject(at=@At("RETURN"), method="getMaxLevel()I", cancellable=true)
	public void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
		if (!FabConf.isEnabled("*.feather_falling_five")) return;
		if (EnchantmentHelperHelper.matches(this, Enchantments.FEATHER_FALLING) && cir.getReturnValueI() < 5) {
			cir.setReturnValue(5);
		}
	}
}
