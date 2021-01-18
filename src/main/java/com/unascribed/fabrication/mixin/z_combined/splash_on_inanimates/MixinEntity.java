package com.unascribed.fabrication.mixin.z_combined.splash_on_inanimates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.SetInvisNoGravReversible;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;

@Mixin(Entity.class)
@EligibleIf(anyConfigEnabled={"*.invisibility_splash_on_inanimates", "*.slowfall_splash_on_inanimates"})
public abstract class MixinEntity implements SetInvisNoGravReversible {

	private boolean fabrication$invisibilityReversible;
	private boolean fabrication$noGravityReversible;
	
	@Shadow
	public World world;
	
	@Shadow
	public abstract boolean isWet();
	
	@Shadow
	public abstract void setInvisible(boolean invisible);
	@Shadow
	public abstract void setNoGravity(boolean noGravity);
	
	@Inject(at=@At("TAIL"), method="baseTick()V", expect=1)
	public void baseTick(CallbackInfo ci) {
		if (!world.isClient && isWet()) {
			if (fabrication$invisibilityReversible) {
				setInvisible(false);
				Object self = this;
				if (self instanceof ArmorStandEntity) {
					((ArmorStandEntity)self).setInvisible(false);
				}
				fabrication$invisibilityReversible = false;
			}
			if (fabrication$noGravityReversible) {
				setNoGravity(false);
				fabrication$noGravityReversible = false;
			}
		}
	}
	
	@Override
	public boolean fabrication$isInvisibilityReversible() {
		return fabrication$invisibilityReversible;
	}
	
	@Override
	public boolean fabrication$isNoGravityReversible() {
		return fabrication$noGravityReversible;
	}
	
	@Override
	public void fabrication$setInvisibilityReversible(boolean reversible) {
		fabrication$invisibilityReversible = reversible;
	}
	
	@Override
	public void fabrication$setNoGravityReversible(boolean reversible) {
		fabrication$noGravityReversible = reversible;
	}
	
	@Inject(at=@At("TAIL"), method="toTag(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;", expect=1)
	public void toTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> ci) {
		if (fabrication$invisibilityReversible) {
			tag.putBoolean("fabrication:InvisibilityReversible", fabrication$invisibilityReversible);
		}
		if (fabrication$noGravityReversible) {
			tag.putBoolean("fabrication:NoGravityReversible", fabrication$noGravityReversible);
		}
	}
	
	@Inject(at=@At("TAIL"), method="fromTag(Lnet/minecraft/nbt/CompoundTag;)V", expect=1)
	public void fromTag(CompoundTag tag, CallbackInfo ci) {
		fabrication$invisibilityReversible = tag.getBoolean("fabrication:InvisibilityReversible");
		fabrication$noGravityReversible = tag.getBoolean("fabrication:NoGravityReversible");
	}
	
}
