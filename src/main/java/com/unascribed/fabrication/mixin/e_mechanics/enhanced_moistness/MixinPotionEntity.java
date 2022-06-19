package com.unascribed.fabrication.mixin.e_mechanics.enhanced_moistness;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.interfaces.MarkWet;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

@Mixin(PotionEntity.class)
@EligibleIf(configAvailable="*.enhanced_moistness")
public abstract class MixinPotionEntity extends ThrownItemEntity {

	public MixinPotionEntity(EntityType<? extends ThrownItemEntity> entityType, double d, double e, double f, World world) {
		super(entityType, d, e, f, world);
	}


	@FabInject(at=@At("TAIL"), method="damageEntitiesHurtByWater()V", locals=LocalCapture.CAPTURE_FAILHARD)
	public void damageEntitiesHurtByWater(CallbackInfo ci, Box box) {
		if (!FabConf.isEnabled("*.enhanced_moistness") || world.isClient) return;
		for (Entity e : world.getEntitiesByClass(Entity.class, box, e -> true)) {
			if (e instanceof MarkWet) {
				((MarkWet)e).fabrication$markWet();
			}
			if (e instanceof EndermanEntity) {
				((EndermanEntity) e).setTarget(null);
			}
		}
	}

}
