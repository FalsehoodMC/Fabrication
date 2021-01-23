package com.unascribed.fabrication.mixin.f_balance.food_always_edible;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.explosion.Explosion.DestructionType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerEntity.class)
@EligibleIf(configEnabled="*.food_always_edible", modNotLoaded = "eternaleats")
public class MixinPlayerEntity {
	@Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
	public void canConsume(boolean ignoreHunger, CallbackInfoReturnable cir) {
		cir.setReturnValue(true);
	}
	
}
