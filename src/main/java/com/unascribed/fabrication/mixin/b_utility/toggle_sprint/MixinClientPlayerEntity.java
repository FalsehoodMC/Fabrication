package com.unascribed.fabrication.mixin.b_utility.toggle_sprint;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.features.FeatureToggleSprint;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;

@Mixin(ClientPlayerEntity.class)
@EligibleIf(configAvailable="*.toggle_sprint", envMatches=Env.CLIENT)
public class MixinClientPlayerEntity {

	@ModifyReturn(target="Lnet/minecraft/client/option/KeyBinding;isPressed()Z", method="tickMovement()V")
	private static boolean fabrication$toggleSprint(boolean old, KeyBinding keyBinding) {
		if (FabConf.isEnabled("*.toggle_sprint") && keyBinding.getTranslationKey().equals("key.sprint") && FeatureToggleSprint.sprinting) {
			return true;
		}
		return old;
	}

}
