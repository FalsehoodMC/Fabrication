package com.unascribed.fabrication.mixin.c_tweaks.bush_walk_doesnt_hurt_with_armor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(SweetBerryBushBlock.class)
@EligibleIf(configAvailable="*.bush_walk_doesnt_hurt_with_armor")
public class MixinSweetBerryBushBlock {

	@Inject(at=@At(value="INVOKE", target="net/minecraft/entity/Entity.damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
			method="onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V", cancellable=true)
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.bush_walk_doesnt_hurt_with_armor") && entity instanceof LivingEntity
				&& ConfigPredicates.shouldRun("*.bush_walk_doesnt_hurt_with_armor", (LivingEntity)entity)) {
			ci.cancel();
		}
	}


}
