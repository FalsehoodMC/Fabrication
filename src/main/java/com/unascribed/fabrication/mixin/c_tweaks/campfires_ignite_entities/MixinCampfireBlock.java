package com.unascribed.fabrication.mixin.c_tweaks.campfires_ignite_entities;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlock.class)
@EligibleIf(configAvailable="*.campfires_ignite_entities")
public class MixinCampfireBlock {

	@FabInject(at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), method="onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	public void damage(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.campfires_ignite_entities")) return;

		entity.setFireTicks(entity.getFireTicks() + 1);
		if (entity.getFireTicks() == 0) entity.setOnFireFor(8);
	}

}
