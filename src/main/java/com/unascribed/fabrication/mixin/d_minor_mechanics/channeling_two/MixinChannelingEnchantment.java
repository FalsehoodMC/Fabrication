package com.unascribed.fabrication.mixin.d_minor_mechanics.channeling_two;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.enchantment.ChannelingEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChannelingEnchantment.class)
@EligibleIf(configAvailable="*.channeling_two")
public class MixinChannelingEnchantment {

	@Inject(at=@At("RETURN"), method="getMaxLevel()I", cancellable=true)
	public void getMaxLevel(CallbackInfoReturnable<Integer> cir) {
		if (!FabConf.isEnabled("*.channeling_two")) return;
		if (cir.getReturnValueI() < 2) {
			cir.setReturnValue(2);
		}
	}

}
