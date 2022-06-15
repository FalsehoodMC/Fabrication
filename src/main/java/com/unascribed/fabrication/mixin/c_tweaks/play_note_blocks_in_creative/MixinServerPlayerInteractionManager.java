package com.unascribed.fabrication.mixin.c_tweaks.play_note_blocks_in_creative;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(ServerPlayerInteractionManager.class)
@EligibleIf(configAvailable="*.play_note_blocks_in_creative")
public class MixinServerPlayerInteractionManager {

	@Shadow
	protected ServerWorld world;
	@Shadow @Final
	protected ServerPlayerEntity player;

	@Inject(at=@At("HEAD"), method="tryBreakBlock(Lnet/minecraft/util/math/BlockPos;)Z", cancellable=true)
	public void tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		if (!FabConf.isEnabled("*.play_note_blocks_in_creative")) return;
		BlockState bs = world.getBlockState(pos);
		if (bs.getBlock() == Blocks.NOTE_BLOCK) {
			if (player.isSneaky()) {
				Blocks.NOTE_BLOCK.onBlockBreakStart(bs, world, pos, player);
				ci.setReturnValue(false);
			}
		}
	}

}
