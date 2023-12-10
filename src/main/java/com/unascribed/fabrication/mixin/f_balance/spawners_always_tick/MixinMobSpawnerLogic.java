package com.unascribed.fabrication.mixin.f_balance.spawners_always_tick;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.spawner.MobSpawnerLogic;

@Mixin(MobSpawnerLogic.class)
@EligibleIf(configAvailable="*.spawners_always_tick")
public class MixinMobSpawnerLogic {

	@Shadow
	private int spawnDelay;

	@FabInject(method="isPlayerInRange(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z", at=@At("RETURN"))
	public void isPlayerInRange(CallbackInfoReturnable<Boolean> cir) {
		// this is called in the mob spawner's update routine only once, so it makes a convenient
		// injection point that doesn't require messing with redirects, weird @Ats, or var capture
		if (FabConf.isEnabled("*.spawners_always_tick") && !cir.getReturnValueZ() && spawnDelay > 0) {
			spawnDelay--;
		}
	}

}
