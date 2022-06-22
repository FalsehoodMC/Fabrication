package com.unascribed.fabrication.mixin.i_woina.no_sprint;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

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

	@FabInject(at=@At("HEAD"), method="setSprinting(Z)V", cancellable = true)
	public void setSprinting(boolean sprinting, CallbackInfo ci) {
		if (FabConf.isEnabled("*.no_sprint")) {
			super.setSprinting(false);
			ci.cancel();
		}
	}
}
