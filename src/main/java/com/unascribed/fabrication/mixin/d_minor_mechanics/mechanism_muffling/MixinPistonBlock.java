package com.unascribed.fabrication.mixin.d_minor_mechanics.mechanism_muffling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.logic.MechanismMuffling;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PistonBlock.class)
@EligibleIf(configAvailable="*.mechanism_muffling")
public class MixinPistonBlock {

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/world/World.playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"),
			method="onSyncedBlockEvent(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;II)Z")
	public void playSound(World subject, PlayerEntity player, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
		if (MixinConfigPlugin.isEnabled("*.mechanism_muffling") && MechanismMuffling.isMuffled(subject, pos)) return;
		subject.playSound(player, pos, sound, category, volume, pitch);
	}
	
}
