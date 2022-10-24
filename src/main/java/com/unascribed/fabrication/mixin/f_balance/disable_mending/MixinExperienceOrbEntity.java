package com.unascribed.fabrication.mixin.f_balance.disable_mending;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(ExperienceOrbEntity.class)
@EligibleIf(configAvailable="*.disable_mending")
public class MixinExperienceOrbEntity {

	private static final Predicate<PlayerEntity> fabrication$disableMendingPredicate = ConfigPredicates.getFinalPredicate("*.disable_mending");
	@Hijack(method="onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V",
			target="Lnet/minecraft/enchantment/EnchantmentHelper;chooseEquipmentWith(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Map$Entry;")
	private static HijackReturn fabrication$no_repair(Enchantment enchantment, PlayerEntity player) {
		if (FabConf.isEnabled("*.disable_mending") && fabrication$disableMendingPredicate.test(player)) return HijackReturn.NULL;
		return null;
	}

}
