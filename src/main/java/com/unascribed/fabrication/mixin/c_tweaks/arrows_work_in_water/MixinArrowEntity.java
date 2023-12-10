package com.unascribed.fabrication.mixin.c_tweaks.arrows_work_in_water;

import com.unascribed.fabrication.FabConf;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.world.World;

@Mixin(ArrowEntity.class)
@EligibleIf(configAvailable="*.arrows_work_in_water")
public abstract class MixinArrowEntity extends PersistentProjectileEntity {

	protected MixinArrowEntity(EntityType<? extends PersistentProjectileEntity> type, World world, ItemStack stack) {
		super(type, world, stack);
	}

	@Override
	protected float getDragInWater() {
		return FabConf.isEnabled("*.arrows_work_in_water") ? 0.85f : super.getDragInWater();
	}

}
