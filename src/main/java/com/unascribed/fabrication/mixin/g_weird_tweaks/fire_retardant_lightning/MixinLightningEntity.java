package com.unascribed.fabrication.mixin.g_weird_tweaks.fire_retardant_lightning;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningEntity.class)
@EligibleIf(configAvailable="*.fire_retardant_lightning")
public class MixinLightningEntity {

	@Inject(at=@At("HEAD"), method="spawnFire(I)V", cancellable=true)
	public void preventFire(int spreadAttempts, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.fire_retardant_lightning")) ci.cancel();
	}

}
