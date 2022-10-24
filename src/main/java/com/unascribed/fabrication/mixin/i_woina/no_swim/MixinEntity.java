package com.unascribed.fabrication.mixin.i_woina.no_swim;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.no_swim")
public class MixinEntity {

	private static final Predicate<Entity> fabrication$noSwimPredicate = ConfigPredicates.getFinalPredicate("*.no_swim");
	@Hijack(method="updateSwimming()V", target="Lnet/minecraft/entity/Entity;isSprinting()Z")
	private static HijackReturn fabrication$disableSwimming(Entity self) {
		return (FabConf.isEnabled("*.no_swim") && fabrication$noSwimPredicate.test(self)) ? HijackReturn.FALSE : null;
	}
}
