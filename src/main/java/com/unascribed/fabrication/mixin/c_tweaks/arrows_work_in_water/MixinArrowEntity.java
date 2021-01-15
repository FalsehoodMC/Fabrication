package com.unascribed.fabrication.mixin.c_tweaks.arrows_work_in_water;

import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.world.World;

@Mixin(ArrowEntity.class)
@EligibleIf(configEnabled="*.arrows_work_in_water")
public abstract class MixinArrowEntity extends PersistentProjectileEntity {


	protected MixinArrowEntity(EntityType<? extends PersistentProjectileEntity> type, double x, double y, double z, World world) {
		super(type, x, y, z, world);
	}

	@Override
	protected float getDragInWater() {
		return MixinConfigPlugin.isEnabled("*.arrows_work_in_water") ? 0.85f : super.getDragInWater();
	}
	
}
