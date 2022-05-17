package com.unascribed.fabrication.mixin.b_utility.disable_villagers;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;

@Mixin(VillagerEntity.class)
@EligibleIf(configAvailable="*.disable_villagers")
public abstract class MixinVillagerEntity extends MerchantEntity {

	public MixinVillagerEntity(EntityType<? extends MerchantEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("HEAD"), method="tick()V", cancellable=true)
	public void tick(CallbackInfo ci) {
		if (FabConf.isEnabled("*.disable_villagers")) {
			discard();
		}
	}

}
