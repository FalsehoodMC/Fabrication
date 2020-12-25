package com.unascribed.fabrication.mixin.g_weird_tweaks.photoresistant_mobs;

import org.spongepowered.asm.mixin.Mixin;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

@Mixin(HostileEntity.class)
@EligibleIf(configEnabled="*.photoresistant_mobs")
public abstract class MixinHostileEntity extends PathAwareEntity {
	
	protected MixinHostileEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	protected boolean isAffectedByDaylight() {
		return RuntimeChecks.check("*.photoresistant_mobs") ? false : super.isAffectedByDaylight();
	}

}
