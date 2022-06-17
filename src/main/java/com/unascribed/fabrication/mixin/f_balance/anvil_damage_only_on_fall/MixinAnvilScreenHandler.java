package com.unascribed.fabrication.mixin.f_balance.anvil_damage_only_on_fall;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabModifyConst;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;

import net.minecraft.screen.AnvilScreenHandler;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configAvailable="*.anvil_damage_only_on_fall", specialConditions=SpecialEligibility.NOT_FORGE)
public class MixinAnvilScreenHandler {

	@FabModifyConst(constant=@Constant(floatValue=0.12f), method="method_24922")
	private static float modifyDamageChance(float chance) {
		return FabConf.isEnabled("*.anvil_damage_only_on_fall") ? 0 : chance;
	}

}
