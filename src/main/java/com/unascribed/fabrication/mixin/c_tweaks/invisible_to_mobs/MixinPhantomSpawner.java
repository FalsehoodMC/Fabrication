package com.unascribed.fabrication.mixin.c_tweaks.invisible_to_mobs;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.gen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PhantomSpawner.class)
@EligibleIf(anyConfigAvailable="*.invisible_to_mobs")
public class MixinPhantomSpawner {

	@ModifyReturn(method="spawn(Lnet/minecraft/server/world/ServerWorld;ZZ)I", target="Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z")
	private static boolean fabrication$taggablePlayersIsSpectator(boolean original, PlayerEntity subject) {
		if (!FabConf.isEnabled("*.invisible_to_mobs")) return original;
		return ConfigPredicates.shouldRun("*.invisible_to_mobs", subject) || original;
	}

}
