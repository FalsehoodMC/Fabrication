package com.unascribed.fabrication.mixin.h_situational.static_dragon_egg;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonEggBlock.class)
@EligibleIf(configAvailable="*.static_dragon_egg")
public class MixinDragonEggBlock {
	
	@Inject(method= "teleport(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
			at=@At("HEAD"), cancellable=true)
	public void isPlayerInRange(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.static_dragon_egg"))
			ci.cancel();
	}
}
