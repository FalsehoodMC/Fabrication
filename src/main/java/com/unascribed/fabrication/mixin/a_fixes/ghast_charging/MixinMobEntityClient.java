package com.unascribed.fabrication.mixin.a_fixes.ghast_charging;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.GhastAttackTime;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;

// MobEntity is our best injection point because GhastEntity doesn't override tick
@Mixin(MobEntity.class)
@EligibleIf(configAvailable="*.ghast_charging", envMatches=Env.CLIENT)
public class MixinMobEntityClient implements GhastAttackTime {

	@Unique
	private int fabrication$ghastAttackTime;

	@Inject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.ghast_charging")) return;
		Object self = this;
		if (self instanceof GhastEntity) {
			GhastEntity g = (GhastEntity)self;
			if (g.isShooting()) {
				if (g.isAlive()) {
					fabrication$ghastAttackTime++;
				}
			} else {
				fabrication$ghastAttackTime = 0;
			}
		}
	}

	@Override
	public int getAttackTime() {
		return fabrication$ghastAttackTime;
	}

}
