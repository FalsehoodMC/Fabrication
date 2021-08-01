package com.unascribed.fabrication.mixin.b_utility.taggable_players;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.logic.PlayerTag;
import com.unascribed.fabrication.support.EligibleIf;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.gen.PhantomSpawner;

@Mixin(PhantomSpawner.class)
@EligibleIf(configEnabled="*.taggable_players")
public class MixinPhantomSpawner {

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/entity/player/PlayerEntity.isSpectator()Z"),
			method="spawn(Lnet/minecraft/server/world/ServerWorld;ZZ)I")
	public boolean spawnIsSpectator(PlayerEntity subject) {
		if (subject instanceof TaggablePlayer && (((TaggablePlayer)subject).fabrication$hasTag(PlayerTag.NO_PHANTOMS) || ((TaggablePlayer)subject).fabrication$hasTag(PlayerTag.INVISIBLE_TO_MOBS))) {
			return true;
		}
		return subject.isSpectator();
	}
	
}
