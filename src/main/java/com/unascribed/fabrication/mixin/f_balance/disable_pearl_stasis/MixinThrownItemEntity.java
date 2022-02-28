package com.unascribed.fabrication.mixin.f_balance.disable_pearl_stasis;


import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.nbt.NbtCompound;

@Mixin(ThrownItemEntity.class)
@EligibleIf(configAvailable="*.disable_pearl_stasis")
public abstract class MixinThrownItemEntity {
	@Inject(at=@At("TAIL"), method= "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void remove_pearl_owner(NbtCompound tag, CallbackInfo ci) {
		if(((Object)this) instanceof EnderPearlEntity && FabConf.isEnabled("*.disable_pearl_stasis"))
			tag.remove("Owner");
	}
}
