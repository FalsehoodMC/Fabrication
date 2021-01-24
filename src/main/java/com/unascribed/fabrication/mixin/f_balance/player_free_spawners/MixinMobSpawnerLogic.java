package com.unascribed.fabrication.mixin.f_balance.player_free_spawners;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.world.MobSpawnerLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobSpawnerLogic.class)
@EligibleIf(configEnabled="*.player_free_spawners")
public class MixinMobSpawnerLogic {
	
	@Inject(method="isPlayerInRange()Z", at=@At(value="HEAD", target="Lnet/minecraft/world/MobSpawnerLogic;update()V"), cancellable=true)
	public void isPlayerInRange(CallbackInfoReturnable<Boolean> cir) {
		if (MixinConfigPlugin.isEnabled("*.player_free_spawners")) {
			cir.setReturnValue(true);
			cir.cancel();
		}
	}
}
