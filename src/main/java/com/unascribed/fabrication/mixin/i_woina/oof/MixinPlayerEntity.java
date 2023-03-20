package com.unascribed.fabrication.mixin.i_woina.oof;

import com.unascribed.fabrication.FabConf;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

@Mixin(value=PlayerEntity.class, priority=1050)
@EligibleIf(configAvailable="*.oof", envMatches=Env.CLIENT)
public abstract class MixinPlayerEntity {

	@FabInject(at=@At("HEAD"), method="getHurtSound(Lnet/minecraft/entity/damage/DamageSource;)Lnet/minecraft/sound/SoundEvent;",
			cancellable=true)
	public void getHurtSound(DamageSource src, CallbackInfoReturnable<SoundEvent> ci) {
		if (!FabConf.isEnabled("*.oof") || !((PlayerEntity)(Object)this).world.isClient) return;
		if (src.isOf(DamageTypes.DROWN)) {
			ci.setReturnValue(SoundEvents.ENTITY_PLAYER_HURT_DROWN);
		} else {
			ci.setReturnValue(FabricationMod.OOF);
		}
	}

}
