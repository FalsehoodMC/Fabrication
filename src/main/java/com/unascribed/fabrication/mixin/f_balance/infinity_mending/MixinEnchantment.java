package com.unascribed.fabrication.mixin.f_balance.infinity_mending;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.infinity_mending")
public abstract class MixinEnchantment {

	@FabInject(at=@At(value="INVOKE", target="Lnet/minecraft/registry/entry/RegistryEntryList;contains(Lnet/minecraft/registry/entry/RegistryEntry;)Z", shift=At.Shift.BEFORE, ordinal=0), method="canBeCombined(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/registry/entry/RegistryEntry;)Z", cancellable=true)
	private static void modify(RegistryEntry<Enchantment> first, RegistryEntry<Enchantment> second, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.infinity_mending")) return;
		if (first.matchesKey(Enchantments.INFINITY) && second.matchesKey(Enchantments.MENDING)) cir.setReturnValue(true);
		if (first.matchesKey(Enchantments.MENDING) && second.matchesKey(Enchantments.INFINITY)) cir.setReturnValue(true);
	}

}
