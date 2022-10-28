package com.unascribed.fabrication.mixin.j_pedantry.entities_cant_climb;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(anyConfigAvailable={"*.entities_cant_climb", "*.creepers_cant_climb"})
public class MixinLivingEntity {

	private static final Predicate<LivingEntity> fabrication$entitiesCantClimbPredicate = ConfigPredicates.getFinalPredicate("*.entities_cant_climb");
	@FabInject(method="isClimbing()Z", at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getBlockPos()Lnet/minecraft/util/math/BlockPos;"), cancellable=true)
	public void isableClimbing(CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isAnyEnabled("*.entities_cant_climb") && fabrication$entitiesCantClimbPredicate.test((LivingEntity)(Object)this)) {
			cir.setReturnValue(false);
		}
	}
}
