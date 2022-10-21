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

@Mixin(LivingEntity.class)
@EligibleIf(anyConfigAvailable={"*.no_phantoms", "*.invisible_to_mobs"})
public abstract class MixinLivingEntity extends Entity {

	protected MixinLivingEntity(EntityType<? extends Entity> entityType, World world) {
		super(entityType, world);
	}

	@FabInject(at=@At("HEAD"), method="canTarget(Lnet/minecraft/entity/LivingEntity;)Z", cancellable=true)
	public void canTarget(LivingEntity other, CallbackInfoReturnable<Boolean> ci) {
		if (!(other instanceof PlayerEntity)) return;
		if (FabConf.isEnabled("*.no_phantoms") && ((Object)this) instanceof PhantomEntity) {
			if (ConfigPredicates.shouldRun("*.no_phantoms", (PlayerEntity)other)) {
				ci.setReturnValue(false);
			}
		}
		if (FabConf.isEnabled("*.invisible_to_mobs") && ConfigPredicates.shouldRun("*.invisible_to_mobs", (PlayerEntity)other)) {
			ci.setReturnValue(false);
		}

	}

}
