package com.unascribed.fabrication.mixin.f_balance.disable_mending_trade;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Optional;

@Mixin(targets = "net.minecraft.village.TradeOffers$EnchantBookFactory")
@EligibleIf(configAvailable="*.disable_mending_trade")
public class MixinEnchantBookFactory {

	@ModifyVariable(at=@At(value= "STORE", ordinal=0), method="create(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/random/Random;)Lnet/minecraft/village/TradeOffer;")
	public Optional<RegistryEntry<Enchantment>> emptyIfMending(Optional<RegistryEntry<Enchantment>> value) {
		if (FabConf.isEnabled("*.disable_mending_trade") && value.isPresent() && value.get().matches(Enchantments.MENDING::equals)) {
			return Optional.empty();
		}
		return value;
	}

}
