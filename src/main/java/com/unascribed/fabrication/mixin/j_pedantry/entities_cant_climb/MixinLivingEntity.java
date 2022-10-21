package com.unascribed.fabrication.mixin.j_pedantry.entities_cant_climb;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.entities_cant_climb")
public class MixinLivingEntity {

	private static final Predicate<LivingEntity> fabrication$entitiesCantClimbPredicate = ConfigPredicates.getFinalPredicate("*.entities_cant_climb");
	@ModifyReturn(method="isClimbing()Z", target="Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/tag/Tag;)Z")
	public boolean fabrication$disableClimbing(boolean old) {
		if (!(old && FabConf.isAnyEnabled("*.entities_cant_climb") && fabrication$entitiesCantClimbPredicate.test((LivingEntity)(Object)this))) return old;
		return false;
	}
}
