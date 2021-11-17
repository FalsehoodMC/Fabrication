package com.unascribed.fabrication.mixin.c_tweaks.nether_cauldron;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CauldronBlock.class)
@EligibleIf(configEnabled="*.nether_cauldron")
public class MixinCauldronBlock {

	@Shadow @Final
	public static IntProperty LEVEL;

	@Inject(at=@At("HEAD"), method="setLevel(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V")
	public void setLevel(World world, BlockPos pos, BlockState state, int level, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.nether_cauldron") && world.getDimension().isUltrawarm() && level > state.get(LEVEL)) {
			world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
			if (world instanceof ServerWorld) {
				((ServerWorld)world).spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX()+0.5, pos.getY()+0.6, pos.getZ()+0.5, 8, 0.2, 0.2, 0.2, 0);
			}
			ci.cancel();
		}
	}
}
