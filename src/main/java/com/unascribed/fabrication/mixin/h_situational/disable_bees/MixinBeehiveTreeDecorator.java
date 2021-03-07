package com.unascribed.fabrication.mixin.h_situational.disable_bees;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.tree.BeehiveTreeDecorator;

@Mixin(BeehiveTreeDecorator.class)
@EligibleIf(configEnabled="*.disable_bees")
public class MixinBeehiveTreeDecorator {

	@Inject(at=@At("HEAD"), method="generate(Lnet/minecraft/world/StructureWorldAccess;Ljava/util/Random;Ljava/util/List;Ljava/util/List;Ljava/util/Set;Lnet/minecraft/util/math/BlockBox;)V",
			cancellable=true)
	public void generate(StructureWorldAccess world, Random random, List<BlockPos> logPositions, List<BlockPos> leavesPositions, Set<BlockPos> placedStates, BlockBox box, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.disable_bees")) {
			ci.cancel();
		}
	}
	
}
