package com.unascribed.fabrication.mixin.g_weird_tweaks.disable_night_skip;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.util.Either;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.disable_night_skip")
public class MixinServerPlayerEntity {

	private static final Predicate<ServerPlayerEntity> fabrication$disableNightSkip = ConfigPredicates.getFinalPredicate("*.disable_night_skip");
	@FabInject(method="trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;",
			at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V", shift=At.Shift.AFTER), cancellable=true)
	public void writeCustomDataToNbt(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> cir) {
		if(FabConf.isEnabled("*.disable_night_skip") && fabrication$disableNightSkip.test((ServerPlayerEntity) (Object) this))
			cir.setReturnValue(Either.left(PlayerEntity.SleepFailureReason.OTHER_PROBLEM));
	}
}
