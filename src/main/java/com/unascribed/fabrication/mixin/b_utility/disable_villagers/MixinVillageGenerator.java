package com.unascribed.fabrication.mixin.b_utility.disable_villagers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.structure.VillageGenerator;

@Mixin(VillageGenerator.class)
@EligibleIf(configAvailable="*.disable_villagers")
public class MixinVillageGenerator {

	@Inject(at=@At("HEAD"), method="init()V", cancellable=true)
	private static void init(CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.disable_villagers")) {
			ci.cancel();
		}
	}

}
