package com.unascribed.fabrication.mixin.e_mechanics.enhanced_moistness;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.MarkWet;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(Entity.class)
@EligibleIf(configEnabled="*.enhanced_moistness")
public abstract class MixinEntity implements MarkWet {

	private int fabrication$wetTimer;
	private boolean fabrication$checkingOriginalWetness;
	
	@Shadow
	public World world;
	@Shadow
	private Vec3d pos;
	
	@Shadow
	public abstract boolean isWet();
	@Shadow
	public abstract boolean isInLava();
	
	@Shadow
	public abstract SoundCategory getSoundCategory();
	@Shadow
	public abstract Box getBoundingBox();
	@Shadow
	public abstract void playSound(SoundEvent se, float volume, float pitch);
	
	@Inject(at=@At("TAIL"), method="baseTick()V", expect=1)
	public void baseTick(CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.enhanced_moistness") || world.isClient) return;
		if (isInLava()) {
			fabrication$wetTimer = 0;
		} else {
			try {
				fabrication$checkingOriginalWetness = true;
				if (isWet()) {
					fabrication$wetTimer = 100;
				} else if (fabrication$wetTimer > 0) {
					fabrication$wetTimer--;
					Object self = this;
					if (self instanceof LivingEntity) {
						if (world.random.nextInt(20) == 0) {
							world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_DROWNED_SWIM, getSoundCategory(), 0.1f, 2f);
						}
						if (fabrication$wetTimer%4 == 0) {
							Box box = getBoundingBox();
							((ServerWorld)world).spawnParticles(ParticleTypes.RAIN, pos.x, pos.y+(box.getYLength()/2), pos.z, 1, box.getXLength()/3, box.getYLength()/4, box.getZLength()/3, 0);
						}
					}
				}
			} finally {
				fabrication$checkingOriginalWetness = false;
			}
		}
	}
	
	@Inject(at=@At("HEAD"), method="isWet()Z", cancellable=true, expect=1)
	public void isWet(CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.enhanced_moistness")) {
			if (fabrication$wetTimer > 0 && !fabrication$checkingOriginalWetness) {
				ci.setReturnValue(true);
			}
		}
	}
	
	@Override
	public void fabrication$markWet() {
		fabrication$wetTimer = 100;
	}
	
}
