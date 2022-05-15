package com.unascribed.fabrication.mixin.i_woina.no_swim;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.no_swim")
public class MixinEntity {

	@Hijack(method="updateSwimming()V", target="Lnet/minecraft/entity/Entity;isSprinting()Z")
	private static HijackReturn fabrication$disableSwimming() {
		return FabConf.isEnabled("*.no_swim") ? new HijackReturn(false) : null;
	}
}
