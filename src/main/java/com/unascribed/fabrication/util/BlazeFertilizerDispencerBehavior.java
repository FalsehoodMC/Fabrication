package com.unascribed.fabrication.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;

//"""forgery reasons"""
public class BlazeFertilizerDispencerBehavior extends ItemDispenserBehavior {
	public static BlazeFertilizerDispencerBehavior INSTANCE = new BlazeFertilizerDispencerBehavior();
	@Override
	protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
		BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
		ServerWorld world = pointer.getWorld();
		BlockState state = world.getBlockState(pos);
		if (state.getBlock().equals(Blocks.NETHER_WART) && state.get(NetherWartBlock.AGE) < 3) {
			world.setBlockState(pos, state.with(NetherWartBlock.AGE, Math.min(world.random.nextInt(3) + state.get(NetherWartBlock.AGE), 3)), 2);
			world.spawnParticles(ParticleTypes.FLAME, pos.getX()+0.5, pos.getY()+0.4, pos.getZ()+0.5, 4, 0.3, 0.3, 0.3, 0.05);
			stack.decrement(1);
		}
		return stack;
	}
}
