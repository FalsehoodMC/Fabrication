package com.unascribed.fabrication.mixin.c_tweaks.less_restrictive_note_blocks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(NoteBlock.class)
@EligibleIf(configEnabled="*.less_restrictive_note_blocks")
public abstract class MixinNoteBlock extends Block {

	public MixinNoteBlock(Settings settings) {
		super(settings);
	}
	
	@Overwrite
	private void playNote(World world, BlockPos pos) {
		boolean topCovered = world.getBlockState(pos.up()).isAir();
		boolean canPlay;
		Direction particleDir = Direction.UP;
		if (RuntimeChecks.check("*.less_restrictive_note_blocks")) {
			canPlay = false;
			Direction[] directions = {
				Direction.UP,
				Direction.NORTH,
				Direction.SOUTH,
				Direction.WEST,
				Direction.EAST,
				Direction.DOWN
			};
			for (Direction dir : directions) {
				BlockPos op = pos.offset(dir);
				BlockState bs = world.getBlockState(op);
				if (!bs.isSideSolidFullSquare(world, op, dir.getOpposite())) {
					particleDir = dir;
					canPlay = true;
					break;
				}
			}
		} else {
			canPlay = topCovered;
		}
		if (canPlay) {
			if (RuntimeChecks.check("*.less_restrictive_note_blocks")) {
				BlockState state = world.getBlockState(pos);
				int note = state.get(NoteBlock.NOTE);
				float pitch = (float) Math.pow(2, (note - 12) / 12D);
				world.playSound(null, pos, state.get(NoteBlock.INSTRUMENT).getSound(), SoundCategory.RECORDS, 3, pitch);
				double pX = pos.getX() + 0.5 + (particleDir.getOffsetX()*0.7);
				double pY = pos.getY() + 0.5 + (particleDir.getOffsetY()*0.7);
				double pZ = pos.getZ() + 0.5 + (particleDir.getOffsetZ()*0.7);
				if (world.isClient) {
					world.addParticle(ParticleTypes.NOTE, pX, pY, pZ, note / 24.0, 0, 0);
				} else {
					((ServerWorld)world).spawnParticles(ParticleTypes.NOTE, pX, pY, pZ, 0, note / 24.0, 0, (particleDir.ordinal() - 1), 1);
				}
			} else {
				world.addSyncedBlockEvent(pos, this, 0, 0);
			}
		}
	}
	
}
