package com.unascribed.fabrication.mixin.f_balance.hunger_is;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
@EligibleIf(anyConfigAvailable={"*.hunger_is_peaceful", "*.hunger_is_easy", "*.hunger_is_normal", "*.hunger_is_hard"})
public class MixinPlayerEntity {


	private static final Predicate<PlayerEntity> fabrication$hungerPeaceful = ConfigPredicates.getFinalPredicate("*.hunger_is_peaceful");
	private static final Predicate<PlayerEntity> fabrication$hungerEasy = ConfigPredicates.getFinalPredicate("*.hunger_is_easy");
	private static final Predicate<PlayerEntity> fabrication$hungerNormal = ConfigPredicates.getFinalPredicate("*.hunger_is_normal");
	private static final Predicate<PlayerEntity> fabrication$hungerHard = ConfigPredicates.getFinalPredicate("*.hunger_is_hard");

	@ModifyReturn(method="tickMovement()V", target="Lnet/minecraft/world/World;getDifficulty()Lnet/minecraft/world/Difficulty;")
	private static Difficulty fabrication$pacefullHunger(Difficulty dif, World world, PlayerEntity pe) {
		if (FabConf.isEnabled("*.hunger_is_hard") && fabrication$hungerHard.test(pe)) return Difficulty.HARD;
		if (FabConf.isEnabled("*.hunger_is_normal") && fabrication$hungerNormal.test(pe)) return Difficulty.NORMAL;
		if (FabConf.isEnabled("*.hunger_is_easy") && fabrication$hungerEasy.test(pe)) return Difficulty.EASY;
		if (FabConf.isEnabled("*.hunger_is_peaceful") && fabrication$hungerPeaceful.test(pe)) return Difficulty.PEACEFUL;
		return dif;
	}

}
