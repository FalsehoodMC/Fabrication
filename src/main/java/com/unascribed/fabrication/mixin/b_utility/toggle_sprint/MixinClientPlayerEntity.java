package com.unascribed.fabrication.mixin.b_utility.toggle_sprint;

import com.unascribed.fabrication.features.FeatureToggleSprint;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
@EligibleIf(configEnabled="*.toggle_sprint", envMatches=Env.CLIENT)
public class MixinClientPlayerEntity {

	@Redirect(at=@At(value="INVOKE", target="Lnet/minecraft/client/option/KeyBinding;isPressed()Z"), method="tickMovement()V")
	public boolean isPressed(KeyBinding keyBinding) {
		if (MixinConfigPlugin.isEnabled("*.toggle_sprint") && keyBinding.getTranslationKey().equals("key.sprint") && FeatureToggleSprint.sprinting) {
			return true;
		}
		return keyBinding.isPressed();
	}
}
