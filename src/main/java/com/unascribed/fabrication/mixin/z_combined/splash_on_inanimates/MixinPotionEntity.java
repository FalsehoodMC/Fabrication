package com.unascribed.fabrication.mixin.z_combined.splash_on_inanimates;

import java.util.List;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.interfaces.SetInvisNoGravReversible;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

@Mixin(PotionEntity.class)
@EligibleIf(anyConfigAvailable={"*.invisibility_splash_on_inanimates", "*.slowfall_splash_on_inanimates"})
public abstract class MixinPotionEntity extends ThrownItemEntity {

	public MixinPotionEntity(EntityType<? extends ThrownItemEntity> entityType, double d, double e, double f, World world) {
		super(entityType, d, e, f, world);
	}

	@FabInject(at=@At("TAIL"), method= "applySplashPotion(Ljava/util/List;Lnet/minecraft/entity/Entity;)V", locals=LocalCapture.CAPTURE_FAILHARD)
	public void applySplashPotion(List<StatusEffectInstance> effects, Entity hit, CallbackInfo ci, Box box) {
		if (!(FabConf.isEnabled("*.invisibility_splash_on_inanimates") || FabConf.isEnabled("*.slowfall_splash_on_inanimates")) || world.isClient) return;
		boolean invis = false;
		boolean slowfall = false;

		for (StatusEffectInstance sei : effects) {
			if (FabConf.isEnabled("*.invisibility_splash_on_inanimates") && sei.getEffectType() == StatusEffects.INVISIBILITY) {
				invis = true;
			}
			if (FabConf.isEnabled("*.slowfall_splash_on_inanimates") && sei.getEffectType() == StatusEffects.SLOW_FALLING) {
				slowfall = true;
			}
		}

		if (invis || slowfall) {
			for (Entity e : world.getEntitiesByClass(Entity.class, box, e -> (!(e instanceof LivingEntity) || e instanceof ArmorStandEntity) && e instanceof SetInvisNoGravReversible)) {
				if (invis) {
					e.setInvisible(true);
					((SetInvisNoGravReversible)e).fabrication$setInvisibilityReversible(true);
				}
				if (slowfall) {
					e.setNoGravity(true);
					((SetInvisNoGravReversible)e).fabrication$setNoGravityReversible(true);
				}
			}
		}
	}

}
