package com.unascribed.fabrication.mixin.g_weird_tweaks.photoresistant_mobs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;

@Mixin(MobEntity.class)
@EligibleIf(configEnabled="*.photoresistant_mobs")
public abstract class MixinMobEntity {

	@Inject(at=@At("HEAD"), method="isAffectedByDaylight()Z", cancellable=true)
	public void isAffectedByDaylight(CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.photoresistant_mobs")) {
			Object self = this;
			if (self instanceof CreeperEntity && MixinConfigPlugin.isEnabled("*.photoallergic_creepers")) return;
			if (self instanceof HostileEntity || self instanceof PhantomEntity) {
				ci.setReturnValue(false);
			}
		}
	}

}
