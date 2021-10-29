package com.unascribed.fabrication.mixin.g_weird_tweaks.leaves_grow_grass;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PlantBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlantBlock.class)
@EligibleIf(configEnabled="*.leaves_grow_grass", envMatches=Env.CLIENT)
public abstract class MixinPlantBlock {

	@Inject(at=@At("HEAD"), method="canPlantOnTop(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z", cancellable=true)
	private void canPlantOnTop(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (!MixinConfigPlugin.isEnabled("*.leaves_grow_grass")) return;
		if (floor.isIn(BlockTags.LEAVES) && (((Object)this) == Blocks.GRASS || ((Object)this) == Blocks.TALL_GRASS)){
			cir.setReturnValue(true);
		}
	}
}
