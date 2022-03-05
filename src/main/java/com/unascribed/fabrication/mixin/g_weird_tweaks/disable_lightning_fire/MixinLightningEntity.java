package com.unascribed.fabrication.mixin.g_weird_tweaks.disable_lightning_fire;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningEntity.class)
@EligibleIf(configAvailable="*.disable_lightning_fire")
public class MixinLightningEntity {

	@Inject(at=@At("HEAD"), method="spawnFire(I)V", cancellable=true)
	public void preventFire(int spreadAttempts, CallbackInfo ci) {
		if (FabConf.isEnabled("*.disable_lightning_fire")) ci.cancel();
	}

}
