package com.unascribed.fabrication;

import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class MechanismMuffling {

	public static boolean isMuffled(World subject, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			if (subject.getBlockState(pos.offset(dir)).isIn(BlockTags.WOOL)) {
				return true;
			}
		}
		return false;
	}

}
