package com.unascribed.fabrication.mixin.c_tweaks.no_phantoms;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import org.spongepowered.asm.mixin.Mixin;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.gen.PhantomSpawner;

@Mixin(PhantomSpawner.class)
@EligibleIf(anyConfigAvailable="*.no_phantoms")
public class MixinPhantomSpawner {

	@ModifyReturn(method="spawn(Lnet/minecraft/server/world/ServerWorld;ZZ)I", target="Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z")
	private static boolean fabrication$taggablePlayersIsSpectator(boolean original, PlayerEntity subject) {
		if (!FabConf.isEnabled("*.no_phantoms")) return original;
		return ConfigPredicates.shouldRun("*.no_phantoms", subject) || original;
	}

}
