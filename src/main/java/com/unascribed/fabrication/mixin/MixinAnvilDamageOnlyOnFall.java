package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.screen.AnvilScreenHandler;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configEnabled="*.anvil_damage_only_on_fall")
public class MixinAnvilDamageOnlyOnFall {

	@ModifyConstant(constant=@Constant(floatValue=0.12f),
			method="method_24922(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V")
	private static float modifyDamageChance(float chance) {
		return RuntimeChecks.check("*.anvil_damage_only_on_fall") ? 0 : chance;
	}
	
}
