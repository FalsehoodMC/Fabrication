package com.unascribed.fabrication.mixin.b_utility.item_despawn;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
@EligibleIf(configEnabled="*.item_despawn")
public abstract class MixinServerWorld {

	@Inject(at=@At("HEAD"), method="addEntity(Lnet/minecraft/entity/Entity;)Z", cancellable=true)
	public void addEntity(Entity e, CallbackInfoReturnable<Boolean> ci) {
		if (e instanceof ItemEntity && e.isRemoved()) {
			// don't squawk about items set to despawn instantly
			ci.setReturnValue(false);
		}
	}
	
	
}
