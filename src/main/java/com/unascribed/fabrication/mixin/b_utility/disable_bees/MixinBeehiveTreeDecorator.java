package com.unascribed.fabrication.mixin.b_utility.disable_bees;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.treedecorator.BeehiveTreeDecorator;

@Mixin(BeehiveTreeDecorator.class)
@EligibleIf(configAvailable="*.disable_bees")
public class MixinBeehiveTreeDecorator {

	@Inject(at=@At("HEAD"), method= "generate(Lnet/minecraft/world/TestableWorld;Ljava/util/function/BiConsumer;Ljava/util/Random;Ljava/util/List;Ljava/util/List;)V",
			cancellable=true)
	public void generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, List<BlockPos> logPositions, List<BlockPos> leavesPositions, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.disable_bees")) {
			ci.cancel();
		}
	}

}
