package com.unascribed.fabrication.mixin.a_fixes.sync_attacker_yaw;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.interfaces.SetAttackerYawAware;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.sync_attacker_yaw")
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}
	
	@Inject(at=@At("HEAD"), method="copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", cancellable=true)
	public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		if (this instanceof SetAttackerYawAware) {
			if (oldPlayer instanceof SetAttackerYawAware) {
				SetAttackerYawAware us = (SetAttackerYawAware)this;
				SetAttackerYawAware them = (SetAttackerYawAware)oldPlayer;
				us.fabrication$setAttackerYawAware(them.fabrication$isAttackerYawAware());
			}
		}
	}
	
}