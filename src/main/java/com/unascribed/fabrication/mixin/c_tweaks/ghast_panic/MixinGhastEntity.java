package com.unascribed.fabrication.mixin.c_tweaks.ghast_panic;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

@Mixin(GhastEntity.class)
@EligibleIf(configAvailable="*.ghast_panic")
public abstract class MixinGhastEntity extends FlyingEntity {

	protected MixinGhastEntity(EntityType<? extends FlyingEntity> arg, World arg2) {
		super(arg, arg2);
	}

	@Inject(at=@At("HEAD"), method="getAmbientSound()Lnet/minecraft/sound/SoundEvent;", cancellable=true)
	public void getAmbientSound(CallbackInfoReturnable<SoundEvent> ci) {
		if (!FabConf.isEnabled("*.ghast_panic")) return;
		if (!world.getDimension().ultrawarm() && world.random.nextInt(8) == 0) {
			ci.setReturnValue(SoundEvents.ENTITY_GHAST_SCREAM);
		}
	}

}
