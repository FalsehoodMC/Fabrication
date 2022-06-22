package com.unascribed.fabrication.mixin.a_fixes.bubble_column_pop;

import java.util.Random;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.block.BlockState;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BubbleColumnBlock.class)
@EligibleIf(configAvailable="*.bubble_column_pop", envMatches=Env.CLIENT)
public class MixinBubbleColumnBlock {

	@Shadow @Final public static BooleanProperty DRAG;

	@FabInject(at=@At("HEAD"), method="randomDisplayTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V")
	public void addBubblePop(BlockState state, World world, BlockPos pos, Random random, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.bubble_column_pop")) return;
		if (!state.get(DRAG) && world.isAir(pos.up())) {
			world.addImportantParticle(ParticleTypes.BUBBLE_POP, pos.getX() + (double)random.nextFloat(), pos.getY() + 1, pos.getZ() + (double)random.nextFloat(), 0.0D, 0.04D, 0.0D);
			world.playSound(pos.getX(), pos.getY()+0.5, pos.getZ(), SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, SoundCategory.BLOCKS, 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
		}
	}

}
