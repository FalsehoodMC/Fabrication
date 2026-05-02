package com.unascribed.fabrication.mixin.c_tweaks.fireproof;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.fireproof")
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	private static final Predicate<PlayerEntity> fabrication$fireproofPredicate = ConfigPredicates.getFinalPredicate("*.fireproof");
	@FabInject(at=@At("HEAD"), method="isInvulnerableTo(Lnet/minecraft/entity/damage/DamageSource;)Z", cancellable=true)
	public void isInvulnerableTo(DamageSource ds, CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isEnabled("*.fireproof")) {
			if (fabrication$fireproofPredicate.test((PlayerEntity)(Object)this) && ds.isFire()) {
				ci.setReturnValue(true);
			}
		}
	}

}
