package com.unascribed.fabrication.mixin.i_woina.no_sprint;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.no_sprint")
abstract public class MixinLivingEntity extends Entity {


	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(at=@At("HEAD"), method="setSprinting(Z)V", cancellable = true)
	public void setSprinting(boolean sprinting, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.no_sprint")) {
			super.setSprinting(false);
			ci.cancel();
		}
	}
}
