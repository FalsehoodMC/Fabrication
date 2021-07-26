package com.unascribed.fabrication.mixin.f_balance.anvil_damage_only_on_fall;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.screen.AnvilScreenHandler;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configEnabled="*.anvil_damage_only_on_fall", specialConditions=SpecialEligibility.NOT_FORGE)
public class MixinAnvilScreenHandler {

	@ModifyConstant(constant=@Constant(floatValue=0.12f), method="method_24922")
	private static float modifyDamageChance(float chance) {
		return MixinConfigPlugin.isEnabled("*.anvil_damage_only_on_fall") ? 0 : chance;
	}
	
}
