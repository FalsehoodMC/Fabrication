package com.unascribed.fabrication.mixin.c_tweaks.invisible_to_mobs;

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
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(anyConfigAvailable={"*.no_phantoms", "*.invisible_to_mobs"})
public abstract class MixinLivingEntity extends Entity {

	protected MixinLivingEntity(EntityType<? extends Entity> entityType, World world) {
		super(entityType, world);
	}

	private static final Predicate<PlayerEntity> fabrication$noPhantomsPredicate = ConfigPredicates.getFinalPredicate("*.no_phantoms");
	private static final Predicate<PlayerEntity> fabrication$invisMobsPredicate = ConfigPredicates.getFinalPredicate("*.invisible_to_mobs");

	@FabInject(at=@At("HEAD"), method="canTarget(Lnet/minecraft/entity/LivingEntity;)Z", cancellable=true)
	public void canTarget(LivingEntity other, CallbackInfoReturnable<Boolean> ci) {
		if (!(other instanceof PlayerEntity)) return;
		if (FabConf.isEnabled("*.no_phantoms") && ((Object)this) instanceof PhantomEntity) {
			if (fabrication$noPhantomsPredicate.test((PlayerEntity)other)) {
				ci.setReturnValue(false);
			}
		}
		if (FabConf.isEnabled("*.invisible_to_mobs") && fabrication$invisMobsPredicate.test((PlayerEntity)other)) {
			ci.setReturnValue(false);
		}

	}

}
