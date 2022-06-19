package com.unascribed.fabrication.mixin.b_utility.disable_villagers;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.structure.VillageGenerator;

@Mixin(VillageGenerator.class)
@EligibleIf(configAvailable="*.disable_villagers")
public class MixinVillageGenerator {

	@FabInject(at=@At("HEAD"), method="init()V", cancellable=true)
	private static void init(CallbackInfo ci) {
		if (FabConf.isEnabled("*.disable_villagers")) {
			ci.cancel();
		}
	}

}
