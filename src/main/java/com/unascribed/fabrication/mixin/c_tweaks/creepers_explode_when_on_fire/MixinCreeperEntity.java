package com.unascribed.fabrication.mixin.c_tweaks.creepers_explode_when_on_fire;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

@Mixin(CreeperEntity.class)
@EligibleIf(configEnabled="*.creepers_explode_when_on_fire")
public abstract class MixinCreeperEntity extends HostileEntity {
	
	protected MixinCreeperEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public void setFireTicks(int ticks) {
		super.setFireTicks(ticks);
		if (MixinConfigPlugin.isEnabled("*.creepers_explode_when_on_fire") && !world.isClient && ticks > 0 && !hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
			ignite();
		}
	}
	
	@Shadow
	public abstract void ignite();

}
