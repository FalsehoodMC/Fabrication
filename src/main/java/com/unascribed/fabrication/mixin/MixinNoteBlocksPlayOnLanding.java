package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.ObserverBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(NoteBlock.class)
@EligibleIf(configEnabled="*.note_blocks_play_on_landing")
public abstract class MixinNoteBlocksPlayOnLanding extends Block {

	public MixinNoteBlocksPlayOnLanding(Settings settings) {
		super(settings);
	}
	
	@Shadow
	private void playNote(World world, BlockPos pos) {}
	
	@Override
	public void onLandedUpon(World world, BlockPos pos, Entity entity, float distance) {
		super.onLandedUpon(world, pos, entity, distance);
		if (!world.isClient && RuntimeChecks.check("*.note_blocks_play_on_landing")) {
			for (int i = 0; i < Math.min(8, Math.ceil(distance/2)); i++) {
				playNote(world, pos);
			}
			if (entity instanceof PlayerEntity) {
				((PlayerEntity)entity).incrementStat(Stats.PLAY_NOTEBLOCK);
			}
			for (Direction dir : Direction.values()) {
				BlockPos theirPos = pos.offset(dir);
				BlockState bs = world.getBlockState(theirPos);
				if (bs.getBlock() == Blocks.OBSERVER) {
					Direction theirDir = bs.get(ObserverBlock.FACING);
					if (theirDir == dir.getOpposite()) {
						bs.scheduledTick((ServerWorld)world, theirPos, world.random);
					}
				}
			}
		}
	}

}
