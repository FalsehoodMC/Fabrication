package com.unascribed.fabrication.mixin.a_fixes.sync_attacker_yaw;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.sync_attacker_yaw", envMatches=Env.CLIENT)
public abstract class MixinLivingEntityClient extends Entity {

	public MixinLivingEntityClient(EntityType<?> type, World world) {
		super(type, world);
	}

	private float fabrication$lastAttackerYaw;

	// actually attackerYaw. has the wrong name in this version of yarn
	@Shadow
	public float knockbackVelocity;

	@Inject(at=@At("HEAD"), method="animateDamage()V")
	public void animateDamageHead(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.sync_attacker_yaw")) return;
		fabrication$lastAttackerYaw = knockbackVelocity;
	}

	@Inject(at=@At("TAIL"), method="animateDamage()V")
	public void animateDamageTail(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.sync_attacker_yaw")) return;
		knockbackVelocity = fabrication$lastAttackerYaw;
	}

	@Inject(at=@At("HEAD"), method="handleStatus(B)V")
	public void handleStatusHead(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.sync_attacker_yaw")) return;
		fabrication$lastAttackerYaw = knockbackVelocity;
	}

	@Inject(at=@At("TAIL"), method="handleStatus(B)V")
	public void handleStatusTail(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.sync_attacker_yaw")) return;
		knockbackVelocity = fabrication$lastAttackerYaw;
	}


}
