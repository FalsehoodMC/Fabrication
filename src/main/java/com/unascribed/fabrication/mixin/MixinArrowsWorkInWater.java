package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.world.World;

@Mixin(ArrowEntity.class)
@EligibleIf(configEnabled="*.arrows_work_in_water")
public abstract class MixinArrowsWorkInWater extends PersistentProjectileEntity {


	protected MixinArrowsWorkInWater(EntityType<? extends PersistentProjectileEntity> type, double x, double y, double z, World world) {
		super(type, x, y, z, world);
	}

	@Override
	protected float getDragInWater() {
		return RuntimeChecks.check("*.arrows_work_in_water") ? 0.85f : super.getDragInWater();
	}
	
}
