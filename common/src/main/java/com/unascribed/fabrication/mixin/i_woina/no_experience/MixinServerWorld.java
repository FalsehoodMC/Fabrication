package com.unascribed.fabrication.mixin.i_woina.no_experience;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
@EligibleIf(configAvailable="*.no_experience")
public class MixinServerWorld {

	@FabInject(at=@At("HEAD"), method="addEntity(Lnet/minecraft/entity/Entity;)Z", cancellable=true)
	public void addEntity(Entity e, CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isEnabled("*.no_experience") && e instanceof ExperienceOrbEntity) {
			ci.setReturnValue(false);
		}
	}

}
