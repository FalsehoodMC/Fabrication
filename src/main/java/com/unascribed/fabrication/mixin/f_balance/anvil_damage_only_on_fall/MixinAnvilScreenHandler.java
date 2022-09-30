package com.unascribed.fabrication.mixin.f_balance.anvil_damage_only_on_fall;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configAvailable="*.anvil_damage_only_on_fall", specialConditions=SpecialEligibility.NOT_FORGE)
public class MixinAnvilScreenHandler {

	@Hijack(target="Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/tag/Tag;)Z",
			method="method_24922(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V")
	private static HijackReturn fabrication$preventAnvilDmg() {
		return FabConf.isEnabled("*.anvil_damage_only_on_fall") ? HijackReturn.FALSE : null;
	}

}
