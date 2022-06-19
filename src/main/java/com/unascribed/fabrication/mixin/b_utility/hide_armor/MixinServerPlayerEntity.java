package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.features.FeatureHideArmor;
import com.unascribed.fabrication.interfaces.GetSuppressedSlots;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.hide_armor")
public abstract class MixinServerPlayerEntity {

	@FabInject(at=@At("TAIL"), method="onSpawn()V")
	public void onSpawn(CallbackInfo ci) {
		FeatureHideArmor.sendSuppressedSlotsForSelf((ServerPlayerEntity)(Object)this);
	}

	@FabInject(at=@At("HEAD"), method="copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", cancellable=true)
	public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		((GetSuppressedSlots)this).fabrication$getSuppressedSlots().clear();
		((GetSuppressedSlots)this).fabrication$getSuppressedSlots().addAll(((GetSuppressedSlots)oldPlayer).fabrication$getSuppressedSlots());
	}

}
