package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.interfaces.MarkWet;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

@Mixin(PotionEntity.class)
@EligibleIf(configEnabled="*.enhanced_moistness")
public abstract class MixinEnhancedMoistnessPotion extends ThrownItemEntity {

	public MixinEnhancedMoistnessPotion(EntityType<? extends ThrownItemEntity> entityType, double d, double e, double f, World world) {
		super(entityType, d, e, f, world);
	}

	
	@Inject(at=@At("TAIL"), method="damageEntitiesHurtByWater()V", locals=LocalCapture.CAPTURE_FAILHARD)
	public void damageEntitiesHurtByWater(CallbackInfo ci, Box box) {
		if (!RuntimeChecks.check("*.enhanced_moistness") || world.isClient) return;
		for (Entity e : world.getEntitiesByClass(Entity.class, box, e -> true)) {
			if (e instanceof MarkWet) {
				((MarkWet)e).fabrication$markWet();
			}
		}
	}
	
}
