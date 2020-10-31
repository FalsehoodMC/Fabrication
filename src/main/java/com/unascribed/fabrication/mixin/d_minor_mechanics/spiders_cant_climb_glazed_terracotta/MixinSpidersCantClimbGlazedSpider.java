package com.unascribed.fabrication.mixin.d_minor_mechanics.spiders_cant_climb_glazed_terracotta;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.SetSlippery;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.world.World;

@Mixin(SpiderEntity.class)
@EligibleIf(configEnabled="*.spiders_cant_climb_glazed_terracotta")
public abstract class MixinSpidersCantClimbGlazedSpider extends HostileEntity implements SetSlippery {

	protected MixinSpidersCantClimbGlazedSpider(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	private boolean fabrication$slippery;
	
	@Shadow
	public abstract void setClimbingWall(boolean b);
	
	@Inject(at=@At("HEAD"), method="tick()V")
	public void tickHead(CallbackInfo ci) {
		fabrication$slippery = false;
	}
	
	@Inject(at=@At("TAIL"), method="tick()V")
	public void tickTail(CallbackInfo ci) {
		if (RuntimeChecks.check("*.spiders_cant_climb_glazed_terracotta") && !world.isClient) {
			setClimbingWall(horizontalCollision && !fabrication$slippery);
		}
	}
	
	@Override
	public void fabrication$setSlippery(boolean slippery) {
		fabrication$slippery = slippery;
	}
	
}
