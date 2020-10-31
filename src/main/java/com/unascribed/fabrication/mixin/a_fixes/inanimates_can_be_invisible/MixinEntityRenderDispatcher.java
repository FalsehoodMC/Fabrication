package com.unascribed.fabrication.mixin.a_fixes.inanimates_can_be_invisible;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Mixin(EntityRenderDispatcher.class)
@EligibleIf(configEnabled="*.inanimates_can_be_invisible", envMatches=Env.CLIENT)
public class MixinEntityRenderDispatcher {

	@Inject(at=@At("HEAD"), method="shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z", cancellable=true)
	public void shouldRender(Entity e, Frustum f, double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
		if (!(e instanceof LivingEntity) && e.isInvisible()) {
			ci.setReturnValue(false);
		}
	}
	
}
