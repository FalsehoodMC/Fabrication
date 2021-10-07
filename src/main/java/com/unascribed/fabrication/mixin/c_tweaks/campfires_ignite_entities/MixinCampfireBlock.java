package com.unascribed.fabrication.mixin.c_tweaks.campfires_ignite_entities;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
@EligibleIf(configAvailable="*.campfires_ignite_mobs")
public class MixinCampfireBlock {

	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), method="onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	public void damage(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.campfires_ignite_mobs")) return;

		entity.setFireTicks(entity.getFireTicks() + 1);
		if (entity.getFireTicks() == 0) entity.setOnFireFor(8);
	}
	
}
