package com.unascribed.fabrication.mixin.g_weird_tweaks.leaves_grow_grass;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(PlantBlock.class)
@EligibleIf(configAvailable="*.leaves_grow_grass")
public abstract class MixinPlantBlock {

	@Inject(at=@At("HEAD"), method="canPlantOnTop(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z", cancellable=true)
	private void canPlantOnTop(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.leaves_grow_grass")) return;
		if (floor.isIn(BlockTags.LEAVES) && (((Object)this) == Blocks.GRASS || ((Object)this) == Blocks.TALL_GRASS)){
			cir.setReturnValue(true);
		}
	}
}
