package com.unascribed.fabrication.mixin.c_tweaks.play_note_blocks_in_creative;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientPlayerInteractionManager.class)
@EligibleIf(configEnabled="*.play_note_blocks_in_creative", envMatches=Env.CLIENT)
public class MixinClientPlayerInteractionManager {

	@Inject(at=@At("HEAD"), method="breakBlock(Lnet/minecraft/util/math/BlockPos;)Z", cancellable=true)
	public void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		if (!RuntimeChecks.check("*.play_note_blocks_in_creative")) return;
		BlockState bs = MinecraftClient.getInstance().world.getBlockState(pos);
		if (bs.getBlock() == Blocks.NOTE_BLOCK) {
			if (MinecraftClient.getInstance().player.isSneaky()) {
				ci.setReturnValue(false);
			}
		}
	}
	
}
