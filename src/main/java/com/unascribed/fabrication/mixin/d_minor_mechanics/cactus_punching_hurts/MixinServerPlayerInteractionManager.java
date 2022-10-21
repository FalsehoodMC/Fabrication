package com.unascribed.fabrication.mixin.d_minor_mechanics.cactus_punching_hurts;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

@Mixin(ServerPlayerInteractionManager.class)
@EligibleIf(configAvailable="*.cactus_punching_hurts")
public class MixinServerPlayerInteractionManager {

	@Shadow @Final protected ServerPlayerEntity player;

	private static final Predicate<PlayerEntity> fabrication$cactusPunchingPredicate = ConfigPredicates.getFinalPredicate("*.cactus_punching_hurts");
	@FabInject(at=@At("HEAD"), method="continueMining(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;I)F")
	private void cactusHurt(BlockState state, BlockPos pos, int i, CallbackInfoReturnable<Float> cir) {
		if (FabConf.isEnabled("*.cactus_punching_hurts") && state.isOf(Blocks.CACTUS) && fabrication$cactusPunchingPredicate.test(player))
			player.damage(DamageSource.CACTUS, 1.0F);
	}

}
