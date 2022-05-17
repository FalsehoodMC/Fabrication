package com.unascribed.fabrication.mixin.f_balance.static_dragon_egg;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(DragonEggBlock.class)
@EligibleIf(configAvailable="*.static_dragon_egg")
public class MixinDragonEggBlock {

	@Inject(method= "teleport(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
			at=@At("HEAD"), cancellable=true)
	public void isPlayerInRange(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
		if (FabConf.isEnabled("*.static_dragon_egg"))
			ci.cancel();
	}
}
