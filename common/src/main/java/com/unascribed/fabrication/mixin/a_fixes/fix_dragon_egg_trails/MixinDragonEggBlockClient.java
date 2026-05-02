package com.unascribed.fabrication.mixin.a_fixes.fix_dragon_egg_trails;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.block.BlockState;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonEggBlock.class)
@EligibleIf(configAvailable="*.fix_dragon_egg_trails", envMatches=Env.CLIENT)
public class MixinDragonEggBlockClient {

	@FabInject(method="teleport(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
			at=@At("HEAD"), cancellable=true)
	public void stopTeleport(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
		if (world.isClient && FabConf.Client.serverHasFabrication() && FabConf.isEnabled("*.fix_dragon_egg_trails"))
			ci.cancel();
	}
}
