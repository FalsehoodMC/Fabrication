package com.unascribed.fabrication.mixin.f_balance.player_free_spawners;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.world.MobSpawnerLogic;

@Mixin(MobSpawnerLogic.class)
@EligibleIf(configAvailable="*.player_free_spawners")
public class MixinMobSpawnerLogic {

	@Inject(method="isPlayerInRange(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", at=@At("HEAD"), cancellable=true)
	public void isPlayerInRange(CallbackInfoReturnable<Boolean> cir) {
		if (MixinConfigPlugin.isEnabled("*.player_free_spawners")) {
			cir.setReturnValue(true);
		}
	}
}
