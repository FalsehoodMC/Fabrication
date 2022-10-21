package com.unascribed.fabrication.mixin.c_tweaks.scares_creepers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.scares_creepers")
public abstract class MixinLivingEntity extends Entity {

	protected MixinLivingEntity(EntityType<? extends Entity> entityType, World world) {
		super(entityType, world);
	}

	private static final Predicate<PlayerEntity> fabrication$scaresCreepersPredicate = ConfigPredicates.getFinalPredicate("*.scares_creepers");
	@FabInject(at=@At("HEAD"), method="canTarget(Lnet/minecraft/entity/LivingEntity;)Z", cancellable=true)
	public void canTarget(LivingEntity other, CallbackInfoReturnable<Boolean> ci) {
		if (!(FabConf.isEnabled("*.scares_creepers") && other instanceof PlayerEntity)) return;
		Object self = this;
		if (self instanceof CreeperEntity) {
			if (fabrication$scaresCreepersPredicate.test((PlayerEntity) other)) {
				ci.setReturnValue(false);
			}
		}

	}

}
