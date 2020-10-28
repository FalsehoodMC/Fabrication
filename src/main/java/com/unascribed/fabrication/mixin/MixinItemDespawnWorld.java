package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
@EligibleIf(specialConditions=SpecialEligibility.ITEM_DESPAWN_NOT_ALL_UNSET)
public abstract class MixinItemDespawnWorld {

	@Inject(at=@At("HEAD"), method="addEntity(Lnet/minecraft/entity/Entity;)Z", cancellable=true)
	public void addEntity(Entity e, CallbackInfoReturnable<Boolean> ci) {
		if (e instanceof ItemEntity && e.removed) {
			// don't squawk about items set to despawn instantly
			ci.setReturnValue(false);
		}
	}
	
	
}
