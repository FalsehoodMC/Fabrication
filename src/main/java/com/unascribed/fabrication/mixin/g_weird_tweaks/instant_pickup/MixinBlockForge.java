package com.unascribed.fabrication.mixin.g_weird_tweaks.instant_pickup;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.InstantPickup;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
@EligibleIf(configAvailable="*.instant_pickup", specialConditions=SpecialEligibility.FORGE)
public class MixinBlockForge {

	@FabInject(at=@At("TAIL"), method="dropResources", remap = false)
	private static void dropStacks(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity breaker, ItemStack stack, boolean dropXp, CallbackInfo ci) {
		if (FabConf.isEnabled("*.instant_pickup") && breaker instanceof PlayerEntity) {
			InstantPickup.slurp(world, new Box(pos).expand(0.25), (PlayerEntity)breaker);
		}
	}


}
