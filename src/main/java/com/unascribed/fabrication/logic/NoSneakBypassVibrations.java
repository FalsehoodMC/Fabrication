package com.unascribed.fabrication.logic;

import com.unascribed.fabrication.interfaces.SetActualBypassState;
import com.unascribed.fabrication.support.injection.FakeMixin;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.entity.Entity;
import net.minecraft.world.event.Vibrations;

@FakeMixin(Vibrations.Callback.class)
public class NoSneakBypassVibrations {
	//Hijack is completely unnecessary, however fakeMixin is a FabInject feature. (tbf also very nice for capture of Entity)
	//Has to be fakeMixin because forgery
	@Hijack(method="canAccept(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/world/event/GameEvent$Emitter;)Z", target="Lnet/minecraft/entity/Entity;bypassesSteppingEffects()Z")
	public static HijackReturn fabrication$getActualBypassesStepping(Entity entity) {
		if (entity instanceof SetActualBypassState) {
			((SetActualBypassState)entity).fabrication$setActualBypassesStepOn();
		}
		return null;
	}
}
