package com.unascribed.fabrication.mixin.b_utility.taggable_players;

import com.unascribed.fabrication.support.injection.ModifyReturn;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.logic.PlayerTag;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.gen.PhantomSpawner;

@Mixin(PhantomSpawner.class)
@EligibleIf(configAvailable="*.taggable_players")
public class MixinPhantomSpawner {

	@ModifyReturn(method="spawn(Lnet/minecraft/server/world/ServerWorld;ZZ)I", target="Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z")
	private static boolean fabrication$taggablePlayersIsSpectator(boolean original, PlayerEntity subject) {
		if (subject instanceof TaggablePlayer && (((TaggablePlayer)subject).fabrication$hasTag(PlayerTag.NO_PHANTOMS) || ((TaggablePlayer)subject).fabrication$hasTag(PlayerTag.INVISIBLE_TO_MOBS))) {
			return true;
		}
		return original;
	}

}
