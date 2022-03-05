package com.unascribed.fabrication.mixin.c_tweaks.creepers_explode_when_on_fire;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

@Mixin(CreeperEntity.class)
@EligibleIf(configAvailable="*.creepers_explode_when_on_fire")
public abstract class MixinCreeperEntity extends HostileEntity {

	protected MixinCreeperEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public void setFireTicks(int ticks) {
		super.setFireTicks(ticks);
		if (FabConf.isEnabled("*.creepers_explode_when_on_fire") && !world.isClient &&
				ConfigPredicates.shouldRun("*.creepers_explode_when_on_fire", (LivingEntity)this)) {
			ignite();
		}
	}

	@Shadow
	public abstract void ignite();

}
