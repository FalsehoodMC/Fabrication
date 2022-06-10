package com.unascribed.fabrication.mixin.z_combined.note_block_tuning;

import java.util.Collection;
import java.util.Iterator;

import com.unascribed.fabrication.FabConf;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.math.IntMath;

import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(NoteBlock.class)
@EligibleIf(anyConfigAvailable={"*.exact_note_block_tuning", "*.note_block_notes", "*.reverse_note_block_tuning"})
public abstract class MixinNoteBlock {

	@Shadow
	protected abstract void playNote(@Nullable Entity entity, World world, BlockPos pos);

	private static final String FABRICATION$NOTE_COLORS = "aa66cccccdd559999bbbaaaaa";
	private static final ImmutableList<String> FABRICATION$NOTES = ImmutableList.of(
			"F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F"
			);
	private static final ImmutableMap<Instrument, String> FABRICATION$INSTRUMENT_NAMES = ImmutableMap.<Instrument, String>builder()
			.put(Instrument.BASS, "String Bass")
			.put(Instrument.SNARE, "Snare Drum")
			.put(Instrument.HAT, "Clicks & Sticks")
			.put(Instrument.BASEDRUM, "Bass Drum/Kick")
			.put(Instrument.BELL, "Bells/Glockenspiel")
			.put(Instrument.FLUTE, "Flute")
			.put(Instrument.CHIME, "Chimes")
			.put(Instrument.GUITAR, "Guitar")
			.put(Instrument.XYLOPHONE, "Xylophone")
			.put(Instrument.IRON_XYLOPHONE, "Vibraphone")
			.put(Instrument.COW_BELL, "Cow Bell")
			.put(Instrument.DIDGERIDOO, "Didgeridoo")
			.put(Instrument.BIT, "Square Wave")
			.put(Instrument.BANJO, "Banjo")
			.put(Instrument.PLING, "Electric Piano")
			.put(Instrument.HARP, "Piano")
			.build();
	private static final ImmutableMap<Instrument, Integer> FABRICATION$INSTRUMENT_OCTAVES = ImmutableMap.<Instrument, Integer>builder()
			// source: own research via ffmpeg showcqt
			.put(Instrument.SNARE, 3)
			.put(Instrument.HAT, 5)
			// bass drum's frequency range is utter nonsense and slides over time :(
			.put(Instrument.BASEDRUM, -1)

			// source: Minecraft Wiki; a few verified via showcqt as a sanity check
			.put(Instrument.BASS, 1)
			.put(Instrument.BELL, 5)
			.put(Instrument.FLUTE, 4)
			.put(Instrument.CHIME, 5)
			.put(Instrument.GUITAR, 2)
			.put(Instrument.XYLOPHONE, 5)
			.put(Instrument.IRON_XYLOPHONE, 3)
			.put(Instrument.COW_BELL, 4)
			.put(Instrument.DIDGERIDOO, 1)
			.put(Instrument.BIT, 3)
			.put(Instrument.BANJO, 3)
			.put(Instrument.PLING, 3)
			.put(Instrument.HARP, 3)
			.build();
	private static final ImmutableMap<Instrument, Integer> FABRICATION$INSTRUMENT_OFFSETS = ImmutableMap.<Instrument, Integer>builder()
			// source: own research via ffmpeg showcqt
			.put(Instrument.SNARE, -2) // starts at E rather than F#
			.put(Instrument.HAT, -9) // starts at A rather than F#
			.build();

	@Inject(at=@At("HEAD"), method= "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", cancellable=true)
	public void onUseHead(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {
		if (!world.isClient) {
			if (FabConf.isEnabled("*.exact_note_block_tuning")) {
				ItemStack stack = player.getStackInHand(hand);
				if (stack.getItem() == Items.STICK) {
					int cur = state.get(NoteBlock.NOTE);
					int nw = Math.min(24, stack.getCount()-1);
					if (cur != nw) {
						int dist;
						if (nw > cur) {
							dist = nw-cur;
						} else {
							dist = nw+(24-cur);
						}
						state = state.with(NoteBlock.NOTE, nw);
						world.setBlockState(pos, state, 3);
						player.increaseStat(Stats.TUNE_NOTEBLOCK, dist);
					}
					playNote(player, world, pos);
					fabrication$informNote(player, state);
					ci.setReturnValue(ActionResult.CONSUME);
					return;
				}
			}
			if (FabConf.isEnabled("*.reverse_note_block_tuning")) {
				if (player.isSneaking()) {
					state = cycleBackward(state, NoteBlock.NOTE);
					world.setBlockState(pos, state, 3);
					playNote(player, world, pos);
					player.incrementStat(Stats.TUNE_NOTEBLOCK);
					fabrication$informNote(player, state);
					ci.setReturnValue(ActionResult.CONSUME);
					return;
				}
			}
		}
	}

	@Inject(at=@At("RETURN"), method="onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;")
	public void onUseReturn(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {
		fabrication$informNote(player, world.getBlockState(pos));
	}

	@Inject(at=@At("HEAD"), method="onBlockBreakStart(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)V")
	public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player, CallbackInfo ci) {
		fabrication$informNote(player, state);
	}

	private void fabrication$informNote(PlayerEntity player, BlockState state) {
		if (!player.world.isClient && FabConf.isEnabled("*.note_block_notes")) {
			int note = state.get(NoteBlock.NOTE);
			Instrument instrument = state.get(NoteBlock.INSTRUMENT);
			char color = FABRICATION$NOTE_COLORS.charAt(note);
			if (FABRICATION$INSTRUMENT_OFFSETS.containsKey(instrument)) {
				note += FABRICATION$INSTRUMENT_OFFSETS.get(instrument);
			}
			int baseOctave = FABRICATION$INSTRUMENT_OCTAVES.get(instrument);
			if (note < 0) {
				baseOctave--;
				note = 12+note;
			}
			String noteStr = FABRICATION$NOTES.get(IntMath.mod(note, FABRICATION$NOTES.size()));
			String octaveStr;
			if (baseOctave == -1) {
				// this instrument has nonsensical frequency mapping, so don't try to print a note
				noteStr = "";
				octaveStr = "";
			} else {
				octaveStr = ((note/12)+baseOctave)+" ";
			}
			player.sendMessage(Text.literal("§" + color + noteStr + octaveStr
					+ FABRICATION$INSTRUMENT_NAMES.get(instrument)
					+ " (" + state.get(NoteBlock.NOTE) + ")"), true);
		}
	}

	@Unique
	private static <S extends BlockState, T extends Comparable<T>> S cycleBackward(S s, Property<T> property) {
		return (S)s.with(property, getPrev(property.getValues(), s.get(property)));
	}

	@Unique
	private static <T> T getPrev(Collection<T> values, T value) {
		T prev = null;
		Iterator<T> iterator = values.iterator();
		while (iterator.hasNext()) {
			T next = iterator.next();
			if (next.equals(value)) {
				if (prev != null) {
					return prev;
				}
			}
			prev = next;
		}
		return prev;
	}

}
