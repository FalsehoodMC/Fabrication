package com.unascribed.fabrication.mixin.c_tweaks.no_sneak_bypass;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.SetActualBypassState;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.no_sneak_bypass")
public abstract class MixinEntity implements SetActualBypassState {

	private boolean fabrication$getActualBypassSteppingEffects = false;

	private static final Predicate<Entity> fabrication$noSneakBypassPredicate = ConfigPredicates.getFinalPredicate("*.no_sneak_bypass");

	public void fabrication$setActualBypassesStepOn() {
		fabrication$getActualBypassSteppingEffects = true;
	}

	@FabInject(method="bypassesSteppingEffects()Z", at=@At("HEAD"), cancellable=true)
	public void dontBypassSteppingEffects(CallbackInfoReturnable<Boolean> cir) {
		if (fabrication$getActualBypassSteppingEffects){
			fabrication$getActualBypassSteppingEffects = false;
			return;
		}
		if (FabConf.isEnabled("*.no_sneak_bypass") && fabrication$noSneakBypassPredicate.test((Entity)(Object)this))
			cir.setReturnValue(false);
	}
}
