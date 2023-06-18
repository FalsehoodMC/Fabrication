package com.unascribed.fabrication.mixin.j_pedantry.entities_cant_climb;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Entity.class)
@EligibleIf(anyConfigAvailable={"*.entities_cant_climb", "*.creepers_cant_climb"})
public class MixinEntity {

	private static final Predicate<LivingEntity> fabrication$entitiesCantClimbPredicate = ConfigPredicates.getFinalPredicate("*.entities_cant_climb");
	@FabInject(method="canClimb(Lnet/minecraft/block/BlockState;)Z", at=@At("HEAD"), cancellable=true)
	private void fabrication$disableClimbing(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isAnyEnabled("*.entities_cant_climb")) return;
		Object self = this;
		if (self instanceof LivingEntity && fabrication$entitiesCantClimbPredicate.test((LivingEntity)self)) {
			cir.setReturnValue(false);
		}
	}
}
