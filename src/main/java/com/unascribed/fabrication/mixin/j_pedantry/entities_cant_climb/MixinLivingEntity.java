package com.unascribed.fabrication.mixin.j_pedantry.entities_cant_climb;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.entities_cant_climb")
public class MixinLivingEntity {

	@ModifyReturn(method="isClimbing()Z", target="Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/tag/TagKey;)Z")
	public boolean fabrication$disableClimbing(boolean old) {
		if (!(old && FabConf.isAnyEnabled("*.entities_cant_climb") && ConfigPredicates.shouldRun("*.entities_cant_climb", (LivingEntity)(Object)this))) return old;
		return false;
	}
}
