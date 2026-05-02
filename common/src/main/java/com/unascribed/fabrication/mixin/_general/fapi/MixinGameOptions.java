package com.unascribed.fabrication.mixin._general.fapi;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.FabricationEventsClient;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
@EligibleIf(specialConditions=SpecialEligibility.NOT_FORGE, envMatches=Env.CLIENT)
public class MixinGameOptions {
		@Mutable @Final @Shadow
		public KeyBinding[] allKeys;

		@FabInject(at=@At("HEAD"), method="load()V")
		public void load(CallbackInfo info) {
			allKeys = FabricationEventsClient.keys(allKeys);
		}

}
