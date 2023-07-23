package com.unascribed.fabrication.mixin.f_balance.no_block_lit_mob_spawning;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(HostileEntity.class)
@EligibleIf(configAvailable="*.no_block_lit_mob_spawning")
public class MixinHostileEntity {

	@FabInject(method="isSpawnDark(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)Z", at=@At("HEAD"), cancellable=true)
	private static void fabrication$oldDimMobSpawning(ServerWorldAccess world, BlockPos pos, Random random, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.no_block_lit_mob_spawning")) return;
		if (world.getLightLevel(LightType.BLOCK, pos) > 0) {
			cir.setReturnValue(false);
		}
	}
}
