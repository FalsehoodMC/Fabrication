package com.unascribed.fabrication.x;

import net.minecraft.block.BlockState;
import net.minecraft.block.ObserverBlock;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class PursurverBlock extends ObserverBlock {

	public static final BooleanProperty POWERED = Properties.POWERED;
	
	public PursurverBlock(Settings settings) {
		super(settings);
	}
	
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		// remove super's normal block update functionality
		return state;
	}

}
