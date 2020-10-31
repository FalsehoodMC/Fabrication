package com.unascribed.fabrication.mixin.d_minor_mechanics.spiders_cant_climb_glazed_terracotta;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.IsSpider;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SpiderEntity;

@Mixin(EntityShapeContext.class)
@EligibleIf(configEnabled="*.spiders_cant_climb_glazed_terracotta")
public abstract class MixinSpidersCantClimbGlazedContext implements IsSpider {

	private boolean fabrication$isSpider;
	
	@Inject(at=@At("TAIL"), method="<init>(Lnet/minecraft/entity/Entity;)V")
	public void construct(Entity e, CallbackInfo ci) {
		this.fabrication$isSpider = e instanceof SpiderEntity;
	}
	
	@Override
	public boolean fabrication$isSpider() {
		return fabrication$isSpider;
	}
	
}
