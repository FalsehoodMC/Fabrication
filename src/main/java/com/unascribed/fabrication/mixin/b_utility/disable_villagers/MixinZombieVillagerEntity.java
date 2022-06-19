package com.unascribed.fabrication.mixin.b_utility.disable_villagers;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.world.World;

@Mixin(ZombieVillagerEntity.class)
@EligibleIf(configAvailable="*.disable_villagers")
public abstract class MixinZombieVillagerEntity extends ZombieEntity {

	public MixinZombieVillagerEntity( EntityType<? extends ZombieEntity> entityType, World world) {
		super(entityType, world);
	}

	@FabInject(at=@At("HEAD"), method="tick()V", cancellable=true)
	public void tick(CallbackInfo ci) {
		if (FabConf.isEnabled("*.disable_villagers")) {
			discard();
		}
	}

}
