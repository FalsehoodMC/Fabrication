package com.unascribed.fabrication.mixin.c_tweaks.no_sneak_bypass;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.no_sneak_bypass")
public abstract class MixinEntity {

	private static final Predicate<Entity> fabrication$noSneakBypassPredicate = ConfigPredicates.getFinalPredicate("*.no_sneak_bypass");
	@ModifyReturn(target="Lnet/minecraft/entity/Entity;bypassesSteppingEffects()Z",
			method="move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V")
	private static boolean fabrication$dontBypassSteppingEffects(boolean old, Entity self) {
		if (FabConf.isEnabled("*.no_sneak_bypass") && fabrication$noSneakBypassPredicate.test(self)) return false;
		return old;
	}
}
