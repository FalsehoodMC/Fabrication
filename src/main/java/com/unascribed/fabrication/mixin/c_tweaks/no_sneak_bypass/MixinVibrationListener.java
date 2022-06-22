package com.unascribed.fabrication.mixin.c_tweaks.no_sneak_bypass;

import com.unascribed.fabrication.interfaces.SetActualBypassState;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.VibrationListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VibrationListener.Callback.class)
@EligibleIf(configAvailable="*.no_sneak_bypass")
public interface MixinVibrationListener {

	@FabInject(method="canAccept(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/world/event/GameEvent$Emitter;)Z",
	at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;bypassesSteppingEffects()Z", shift=At.Shift.BEFORE))
	private void fabrication$getActualBypassesStepping(GameEvent gameEvent, GameEvent.Emitter emitter, CallbackInfoReturnable<Boolean> cir) {
		Entity entity = emitter.sourceEntity();
		if (entity instanceof SetActualBypassState) {
			 ((SetActualBypassState)entity).fabrication$setActualBypassesStepOn();
		}
	}

}
