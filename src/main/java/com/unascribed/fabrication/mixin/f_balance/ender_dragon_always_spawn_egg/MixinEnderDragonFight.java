package com.unascribed.fabrication.mixin.f_balance.ender_dragon_always_spawn_egg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;

@Mixin(EnderDragonFight.class)
@EligibleIf(configAvailable="*.ender_dragon_always_spawn_egg")
public class MixinEnderDragonFight {
	@Shadow
	private boolean previouslyKilled;

	@Inject(method="dragonKilled(Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;)V", at=@At(value="INVOKE", target="Lnet/minecraft/entity/boss/dragon/EnderDragonFight;generateNewEndGateway()V"))
	public void dragonKilled(EnderDragonEntity dragon, CallbackInfo ci){
		if (MixinConfigPlugin.isEnabled("*.ender_dragon_always_spawn_egg")){
			previouslyKilled = false;
		}
	}
}
