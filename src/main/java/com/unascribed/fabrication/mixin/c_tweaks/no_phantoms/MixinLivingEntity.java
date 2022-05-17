package com.unascribed.fabrication.mixin.c_tweaks.no_phantoms;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.no_phantoms")
public abstract class MixinLivingEntity extends Entity {

	protected MixinLivingEntity(EntityType<? extends Entity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("HEAD"), method="canTarget(Lnet/minecraft/entity/LivingEntity;)Z", cancellable=true)
	public void canTarget(LivingEntity other, CallbackInfoReturnable<Boolean> ci) {
		if (!(FabConf.isEnabled("*.no_phantoms") && other instanceof PlayerEntity)) return;
		Object self = this;
		if (self instanceof PhantomEntity) {
			if (ConfigPredicates.shouldRun("*.no_phantoms", (PlayerEntity)other)) {
				ci.setReturnValue(false);
			}
		}

	}

}
