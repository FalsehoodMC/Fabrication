package com.unascribed.fabrication.mixin.c_tweaks.less_restrictive_note_blocks;

import com.unascribed.fabrication.FabConf;
import net.minecraft.entity.Entity;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoteBlock.class)
@EligibleIf(configAvailable="*.less_restrictive_note_blocks")
public abstract class MixinNoteBlock extends Block {

	public MixinNoteBlock(Settings settings) {
		super(settings);
	}

	@FabInject(method="playNote(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at=@At("HEAD"), cancellable=true)
	private void playNote(Entity entity, BlockState s1, World world, BlockPos pos, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.less_restrictive_note_blocks")) return;
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
				BlockState state = world.getBlockState(pos);
				int note = state.get(NoteBlock.NOTE);
				float pitch = (float) Math.pow(2, (note - 12) / 12D);
				world.playSound(null, pos, state.get(NoteBlock.INSTRUMENT).getSound().value(), SoundCategory.RECORDS, 3, pitch);
				double pX = pos.getX() + 0.5 + (dir.getOffsetX()*0.7);
				double pY = pos.getY() + 0.5 + (dir.getOffsetY()*0.7);
				double pZ = pos.getZ() + 0.5 + (dir.getOffsetZ()*0.7);
				if (world.isClient) {
					world.addParticle(ParticleTypes.NOTE, pX, pY, pZ, note / 24.0, 0, 0);
				} else {
					((ServerWorld)world).spawnParticles(ParticleTypes.NOTE, pX, pY, pZ, 0, note / 24.0, 0, (dir.ordinal() - 1), 1);
				}
				world.emitGameEvent(entity, GameEvent.NOTE_BLOCK_PLAY, pos);
				break;
			}
		}
		ci.cancel();
	}

}
