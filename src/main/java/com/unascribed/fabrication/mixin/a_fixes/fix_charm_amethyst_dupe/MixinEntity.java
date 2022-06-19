package com.unascribed.fabrication.mixin.a_fixes.fix_charm_amethyst_dupe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.SetInvisibleByCharm;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.fix_charm_amethyst_dupe", modLoaded="charm")
public abstract class MixinEntity implements SetInvisibleByCharm {

	private boolean fabrication$invisibleByCharm;

	@Override
	public boolean fabrication$isInvisibleByCharm() {
		return fabrication$invisibleByCharm;
	}

	@Override
	public void fabrication$setInvisibleByCharm(boolean b) {
		fabrication$invisibleByCharm = b;
	}

	@FabInject(at=@At("TAIL"), method="writeNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/nbt/NbtCompound;")
	public void toTag(NbtCompound tag, CallbackInfoReturnable<NbtCompound> ci) {
		if (fabrication$invisibleByCharm) {
			tag.putBoolean("fabrication:InvisibleByCharm", fabrication$invisibleByCharm);
		}
	}

	@FabInject(at=@At("TAIL"), method="readNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void fromTag(NbtCompound tag, CallbackInfo ci) {
		fabrication$invisibleByCharm = tag.getBoolean("fabrication:InvisibleByCharm");
	}

}
