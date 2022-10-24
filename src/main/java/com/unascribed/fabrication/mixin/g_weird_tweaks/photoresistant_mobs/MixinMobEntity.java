package com.unascribed.fabrication.mixin.g_weird_tweaks.photoresistant_mobs;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;

@Mixin(MobEntity.class)
@EligibleIf(configAvailable="*.photoresistant_mobs")
public abstract class MixinMobEntity {

	@FabInject(at=@At("HEAD"), method="isAffectedByDaylight()Z", cancellable=true)
	public void isAffectedByDaylight(CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isEnabled("*.photoresistant_mobs")) {
			Object self = this;
			if (self instanceof CreeperEntity && FabConf.isEnabled("*.photoallergic_creepers")) return;
			if (self instanceof HostileEntity || self instanceof PhantomEntity) {
				ci.setReturnValue(false);
			}
		}
	}

}
