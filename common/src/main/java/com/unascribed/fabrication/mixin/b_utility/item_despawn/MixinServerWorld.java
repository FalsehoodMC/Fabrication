package com.unascribed.fabrication.mixin.b_utility.item_despawn;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
@EligibleIf(configAvailable="*.item_despawn")
public abstract class MixinServerWorld {

	@FabInject(at=@At("HEAD"), method="addEntity(Lnet/minecraft/entity/Entity;)Z", cancellable=true)
	public void addEntity(Entity e, CallbackInfoReturnable<Boolean> ci) {
		if (e instanceof ItemEntity && e.isRemoved()) {
			// don't squawk about items set to despawn instantly
			ci.setReturnValue(false);
		}
	}


}
