package com.unascribed.fabrication.mixin.i_woina.no_sprint;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.no_sprint")
public abstract class MixinEntity {
	private static final Predicate<Entity> fabrication$noSprintPredicate = ConfigPredicates.getFinalPredicate("*.no_sprint");

	@FabInject(at=@At("RETURN"), method="updateSwimming()V")
	public void setSprinting(CallbackInfo ci) {
		Entity entity = (Entity)(Object)this;
		if (!entity.isSwimming() && FabConf.isEnabled("*.no_sprint") && fabrication$noSprintPredicate.test(entity)) {
			entity.setSprinting(false);
		}
	}
}
