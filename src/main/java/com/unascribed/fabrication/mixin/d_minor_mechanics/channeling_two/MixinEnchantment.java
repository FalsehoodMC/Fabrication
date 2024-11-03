package com.unascribed.fabrication.mixin.d_minor_mechanics.channeling_two;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.channeling_two")
public class MixinEnchantment {

	@FabInject(at=@At("RETURN"), method="getMaxLevel()I", cancellable=true)
	public void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
		if (!FabConf.isEnabled("*.channeling_two")) return;
		if (EnchantmentHelperHelper.matches(this, Enchantments.CHANNELING) && cir.getReturnValueI() < 2) {
			cir.setReturnValue(2);
		}
	}

}
