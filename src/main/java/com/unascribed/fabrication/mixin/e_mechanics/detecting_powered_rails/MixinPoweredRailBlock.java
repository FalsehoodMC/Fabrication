package com.unascribed.fabrication.mixin.e_mechanics.detecting_powered_rails;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@Mixin(PoweredRailBlock.class)
@EligibleIf(configAvailable="*.detecting_powered_rails")
public abstract class MixinPoweredRailBlock extends AbstractRailBlock {

	protected MixinPoweredRailBlock(boolean allowCurves, Settings settings) {
		super(allowCurves, settings);
	}

	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return MixinConfigPlugin.isEnabled("*.detecting_powered_rails");
	}

	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return MixinConfigPlugin.isEnabled("*.detecting_powered_rails");
	}

	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		if (!MixinConfigPlugin.isEnabled("*.detecting_powered_rails")) return 0;
		return Blocks.DETECTOR_RAIL.getComparatorOutput(Blocks.DETECTOR_RAIL.getDefaultState().with(DetectorRailBlock.POWERED, true), world, pos);
	}
	
	@Override
	public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		if (!MixinConfigPlugin.isEnabled("*.detecting_powered_rails")) return 0;
		if (!(world instanceof World)) return 0;
		BlockState who = world.getBlockState(pos.offset(direction.getOpposite()));
		if (!who.isOf(Blocks.REPEATER)) return 0;
		if (who.get(RepeaterBlock.FACING) != direction) return 0;
		return ((AccessorDetectorRailBlock)Blocks.DETECTOR_RAIL).fabrication$getCarts((World)world, pos, AbstractMinecartEntity.class, e -> true).isEmpty() ? 0 : 15;
	}
	
	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (!MixinConfigPlugin.isEnabled("*.detecting_powered_rails")) return;
		world.updateComparators(pos, this);
		for (Direction d : Direction.Type.HORIZONTAL) {
			BlockPos ofs = pos.offset(d);
			BlockState bs = world.getBlockState(ofs);
			if (bs.getBlock() == Blocks.REPEATER && bs.get(RepeaterBlock.FACING) == d.getOpposite()) {
				bs.neighborUpdate(world, ofs, this, pos, false);
			}
		}
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (!MixinConfigPlugin.isEnabled("*.detecting_powered_rails")) return;
		if (!world.isClient) {
			if (entity instanceof AbstractMinecartEntity) {
				for (Direction d : Direction.Type.HORIZONTAL) {
					BlockPos ofs = pos.offset(d);
					if (!world.isChunkLoaded(ofs)) continue;
					BlockState bs = world.getBlockState(ofs);
					if (bs.getBlock() == Blocks.REPEATER && bs.get(RepeaterBlock.FACING) == d.getOpposite()) {
						bs.neighborUpdate(world, ofs, this, pos, false);
					}
				}
			}
			world.getBlockTickScheduler().schedule(pos, this, 20);
			world.updateComparators(pos, this);
		}
	}

	
}
