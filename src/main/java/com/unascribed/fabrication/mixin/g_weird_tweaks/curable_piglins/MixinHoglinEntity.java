package com.unascribed.fabrication.mixin.g_weird_tweaks.curable_piglins;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.ZombImmunizableEntity;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoglinEntity.class)
@EligibleIf(configAvailable="*.curable_piglins")
public class MixinHoglinEntity implements ZombImmunizableEntity {

	public boolean fabrication$isImmuneToZombification = false;

	@Inject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putBoolean("fabrication$isImmuneToZombification", fabrication$isImmuneToZombification);
	}

	@Inject(at=@At("HEAD"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		fabrication$isImmuneToZombification = nbt.getBoolean("fabrication$isImmuneToZombification");
	}

	@Override
	public void fabrication$setZombImmune(boolean zombImmune) {
		fabrication$isImmuneToZombification = zombImmune;
	}

	@Override
	public boolean fabrication$isZombImmune() {
		return fabrication$isImmuneToZombification;
	}


	@Inject(at=@At("HEAD"), method="isImmuneToZombification()Z", cancellable=true)
	public void isImmuneToZombification(CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.curable_piglins") && fabrication$isImmuneToZombification) cir.setReturnValue(true);
	}
}
