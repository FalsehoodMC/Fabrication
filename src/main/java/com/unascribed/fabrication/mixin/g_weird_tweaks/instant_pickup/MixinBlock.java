package com.unascribed.fabrication.mixin.g_weird_tweaks.instant_pickup;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.logic.InstantPickup;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

@Mixin(Block.class)
@EligibleIf(configAvailable="*.instant_pickup")
public class MixinBlock {

	@Inject(at=@At("TAIL"), method="dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V")
	private static void dropStacks(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity breaker, ItemStack stack, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.instant_pickup") && breaker instanceof PlayerEntity) {
			InstantPickup.slurp(world, new Box(pos).expand(0.25), (PlayerEntity)breaker);
		}
	}
	
	
}
