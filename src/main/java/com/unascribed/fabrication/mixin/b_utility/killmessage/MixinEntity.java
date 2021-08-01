package com.unascribed.fabrication.mixin.b_utility.killmessage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.GetKillMessage;
import com.unascribed.fabrication.support.EligibleIf;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.killmessage")
public abstract class MixinEntity implements GetKillMessage {
	
	private String fabrication$killmessage = null;
	
	@Override
	public String fabrication$getKillMessage() {
		return fabrication$killmessage;
	}
	
	@Inject(at=@At("TAIL"), method="writeNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/nbt/NbtCompound;")
	public void toTag(NbtCompound tag, CallbackInfoReturnable<NbtCompound> ci) {
		if (fabrication$killmessage != null) {
			tag.putString("KillMessage", fabrication$killmessage);
		}
	}
	
	@Inject(at=@At("TAIL"), method="readNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void fromTag(NbtCompound tag, CallbackInfo ci) {
		if (tag.contains("KillMessage", NbtType.STRING) ) {
			fabrication$killmessage = tag.getString("KillMessage");
		}
	}
	
}
