package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CactusBlock.class)
@EligibleIf(configEnabled="*.cactus_walk_doesnt_hurt_with_boots")
public class MixinCactusWalkDoesntHurtWithBoots {

	@Inject(at=@At("HEAD"), method="onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V", cancellable=true)
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (RuntimeChecks.check("*.cactus_walk_doesnt_hurt_with_boots") && entity instanceof LivingEntity && (int)(entity.getPos().y+0.075) > pos.getY()) {
			LivingEntity le = (LivingEntity)entity;
			if (!le.getEquippedStack(EquipmentSlot.FEET).isEmpty()) {
				ci.cancel();
			}
		}
	}
	
}
