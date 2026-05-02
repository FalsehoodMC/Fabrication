package com.unascribed.fabrication.interfaces;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;

public interface SetPreZombified {
	void fabrication$setPreZombifiedType(EntityType<? extends MobEntity> type);
}
