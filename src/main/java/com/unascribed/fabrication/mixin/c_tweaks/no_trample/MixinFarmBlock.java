package com.unascribed.fabrication.mixin.c_tweaks.no_trample;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.Block;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Predicate;

@Mixin(FarmlandBlock.class)
@EligibleIf(anyConfigAvailable={"*.feather_falling_no_trample", "*.no_trample"})
public abstract class MixinFarmBlock extends Block {

	public MixinFarmBlock(Settings settings) {
		super(settings);
	}

	private static final Predicate<LivingEntity> fabrication$noTramplePredicate = ConfigPredicates.getFinalPredicate("*.no_trample");
	@FabInject(method="onLandedUpon(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V",
			at=@At("HEAD"), cancellable=true)
	public void onLandedUpon(World world, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
		if (!FabConf.isAnyEnabled("*.no_trample")) return;
		if (entity instanceof LivingEntity && fabrication$noTramplePredicate.test((LivingEntity)entity)) {
			super.onLandedUpon(world, pos, entity, fallDistance);
			ci.cancel();
		}
	}
}
