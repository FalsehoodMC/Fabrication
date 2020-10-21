package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.unascribed.fabrication.support.OnlyIf;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

@Mixin(CreeperEntity.class)
@OnlyIf(config="tweaks.creepers_explode_when_on_fire")
public abstract class MixinCreepersExplodeWhenOnFire extends HostileEntity {
	
	protected MixinCreepersExplodeWhenOnFire(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public void setFireTicks(int ticks) {
		super.setFireTicks(ticks);
		if (ticks > 0) {
			ignite();
		}
	}
	
	@Shadow
	public abstract void ignite();

}
