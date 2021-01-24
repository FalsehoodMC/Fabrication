package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.features.FeatureHideArmor;
import com.unascribed.fabrication.interfaces.GetSuppressedSlots;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configEnabled="*.hide_armor")
public abstract class MixinServerPlayerEntity {

	@Inject(at=@At("TAIL"), method="sendInitialChunkPackets(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/network/Packet;Lnet/minecraft/network/Packet;)V",
			expect=1)
	public void sendInitialChunkPackets(ChunkPos pos, Packet<?> pkt1, Packet<?> pkt2, CallbackInfo ci) {
		FeatureHideArmor.sendSuppressedSlotsForSelf((ServerPlayerEntity)(Object)this);
	}
	
	@Inject(at=@At("HEAD"), method="copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", cancellable=true, expect=1)
	public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		((GetSuppressedSlots)this).fabrication$getSuppressedSlots().clear();
		((GetSuppressedSlots)this).fabrication$getSuppressedSlots().addAll(((GetSuppressedSlots)oldPlayer).fabrication$getSuppressedSlots());
	}
	
}
