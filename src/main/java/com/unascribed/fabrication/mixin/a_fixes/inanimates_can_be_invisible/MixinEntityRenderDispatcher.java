package com.unascribed.fabrication.mixin.a_fixes.inanimates_can_be_invisible;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
@EligibleIf(configAvailable="*.inanimates_can_be_invisible", envMatches=Env.CLIENT)
public abstract class MixinEntityRenderDispatcher {

	@FabInject(at=@At("HEAD"), method="shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z", cancellable=true)
	public void shouldRender(Entity e, Frustum f, double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
		if (e instanceof LivingEntity) return;
		if (e.isInvisible()) {
			if (!FabConf.isEnabled("*.inanimates_can_be_invisible")) return;
			if (!e.getClass().isAssignableFrom(ItemFrameEntity.class)) {
				ci.setReturnValue(false);
			}
		}
	}

}
