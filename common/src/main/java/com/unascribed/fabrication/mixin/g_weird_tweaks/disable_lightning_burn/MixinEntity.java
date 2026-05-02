package com.unascribed.fabrication.mixin.g_weird_tweaks.disable_lightning_burn;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.disable_lightning_burn")
public class MixinEntity {


	@Hijack(method="onStruckByLightning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LightningEntity;)V", target="Lnet/minecraft/entity/Entity;setFireTicks(I)V")
	private static boolean fabrication$preventFire1() {
		return FabConf.isEnabled("*.disable_lightning_burn");
	}
	@Hijack(method="onStruckByLightning(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LightningEntity;)V", target="Lnet/minecraft/entity/Entity;setOnFireFor(I)V")
	private static boolean fabrication$preventFire2() {
		return FabConf.isEnabled("*.disable_lightning_burn");
	}
}
