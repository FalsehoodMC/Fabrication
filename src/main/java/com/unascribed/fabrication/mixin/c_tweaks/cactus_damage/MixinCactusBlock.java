package com.unascribed.fabrication.mixin.c_tweaks.cactus_damage;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Predicate;

@Mixin(CactusBlock.class)
@EligibleIf(anyConfigAvailable={"*.cactus_walk_doesnt_hurt_with_boots", "*.cactus_brush_doesnt_hurt_with_chest"})
public class MixinCactusBlock {

	private static final Predicate<LivingEntity> fabrication$cactusWalkWithBoots = ConfigPredicates.getFinalPredicate("*.cactus_walk_doesnt_hurt_with_boots");
	private static final Predicate<LivingEntity> fabrication$cactusWalkWithChest = ConfigPredicates.getFinalPredicate("*.cactus_brush_doesnt_hurt_with_chest");

	@FabInject(at=@At("HEAD"), method="onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V", cancellable=true)
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (!(entity instanceof LivingEntity)) return;
		LivingEntity le = (LivingEntity)entity;
		boolean touchedTop = (int)(entity.getPos().y+0.075) > pos.getY();
		if (touchedTop) {
			if (FabConf.isEnabled("*.cactus_walk_doesnt_hurt_with_boots")) {
				if (fabrication$cactusWalkWithBoots.test(le)) {
					ci.cancel();
				}
			}
		} else {
			if (FabConf.isEnabled("*.cactus_brush_doesnt_hurt_with_chest")) {
				if (fabrication$cactusWalkWithChest.test(le)) {
					ci.cancel();
				}
			}
		}
	}

}
