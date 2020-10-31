package com.unascribed.fabrication.mixin.z_combined.note_block_tuning;

import java.util.Collection;
import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Property;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(NoteBlock.class)
@EligibleIf(anyConfigEnabled={"*.exact_note_block_tuning", "*.note_block_notes", "*.reverse_note_block_tuning"})
public class MixinNoteBlock {
	
	private static final ImmutableList<String> FABRICATION$NOTES = ImmutableList.of(
			"§aF#",
			"§aG",
			"§6G#",
			"§6A",
			"§cA#",
			"§cB",
			"§cC",
			"§cC#",
			"§cD",
			"§dD#",
			"§dE",
			"§5F",
			"§5F#",
			"§9G",
			"§9G#",
			"§9A",
			"§9A#",
			"§bB",
			"§bC",
			"§bC#",
			"§aD",
			"§aD#",
			"§aE",
			"§aF",
			"§aF#"
	);
	private static final ImmutableMap<Instrument, String> FABRICATION$INSTRUMENTS = ImmutableMap.<Instrument, String>builder()
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
	
	@Shadow
	private void playNote(World world, BlockPos pos) {}
	
	@Inject(at=@At("HEAD"), method= "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", cancellable=true)
	public void onUseHead(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {
		if (!world.isClient) {
			if (MixinConfigPlugin.isEnabled("*.exact_note_block_tuning")) {
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
					playNote(world, pos);
					fabrication$informNote(player, state);
					ci.setReturnValue(ActionResult.CONSUME);
					return;
				}
			}
			if (MixinConfigPlugin.isEnabled("*.reverse_note_block_tuning")) {
				if (player.isSneaking()) {
					state = cycleBackward(state, NoteBlock.NOTE);
					world.setBlockState(pos, state, 3);
					playNote(world, pos);
					player.incrementStat(Stats.TUNE_NOTEBLOCK);
					fabrication$informNote(player, state);
					ci.setReturnValue(ActionResult.CONSUME);
					return;
				}
			}
		}
	}
	
	@Inject(at=@At("RETURN"), method="onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", cancellable=true)
	public void onUseReturn(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {
		fabrication$informNote(player, world.getBlockState(pos));
	}
	
	@Inject(at=@At("HEAD"), method="onBlockBreakStart(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)V")
	public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player, CallbackInfo ci) {
		fabrication$informNote(player, state);
	}

	private void fabrication$informNote(PlayerEntity player, BlockState state) {
		if (!player.world.isClient && MixinConfigPlugin.isEnabled("*.note_block_notes")) {
			player.sendMessage(new LiteralText(FABRICATION$NOTES.get(state.get(NoteBlock.NOTE))
					+" "+FABRICATION$INSTRUMENTS.get(state.get(NoteBlock.INSTRUMENT))
					+" ("+state.get(NoteBlock.NOTE)+")"), true);
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
