package com.unascribed.fabrication.mixin.c_tweaks.invisible_to_mobs;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.gen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(PhantomSpawner.class)
@EligibleIf(anyConfigAvailable={"*.no_phantoms", "*.invisible_to_mobs"})
public class MixinPhantomSpawner {

	private static final Predicate<PlayerEntity> fabrication$noPhantomsPredicate = ConfigPredicates.getFinalPredicate("*.no_phantoms");
	private static final Predicate<PlayerEntity> fabrication$invisMobsPredicate = ConfigPredicates.getFinalPredicate("*.invisible_to_mobs");

	@ModifyReturn(method="spawn(Lnet/minecraft/server/world/ServerWorld;ZZ)I", target="Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z")
	private static boolean fabrication$InvisTaggablePlayersIsSpectator(boolean original, PlayerEntity subject) {
		if (FabConf.isEnabled("*.invisible_to_mobs") && fabrication$invisMobsPredicate.test(subject)) return true;
		if (FabConf.isEnabled("*.no_phantoms") && fabrication$noPhantomsPredicate.test(subject)) return true;
		return original;
	}

}
