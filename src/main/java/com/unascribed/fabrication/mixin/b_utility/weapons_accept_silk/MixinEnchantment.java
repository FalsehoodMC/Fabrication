package com.unascribed.fabrication.mixin.b_utility.weapons_accept_silk;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.weapons_accept_silk")
public abstract class MixinEnchantment {

	@FabInject(at=@At("HEAD"), method="canBeCombined(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/registry/entry/RegistryEntry;)Z", cancellable=true)
	private static void canCombine(RegistryEntry<Enchantment> first, RegistryEntry<Enchantment> second, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.weapons_accept_silk")) return;
		if (first.matchesKey(Enchantments.LOOTING) && second.matchesKey(Enchantments.SILK_TOUCH)) cir.setReturnValue(false);
		if (first.matchesKey(Enchantments.SILK_TOUCH) && second.matchesKey(Enchantments.LOOTING)) cir.setReturnValue(false);
	}
	@FabInject(at=@At("HEAD"), method="isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	private void isacceptable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.weapons_accept_silk")) return;
		if (!EnchantmentHelperHelper.matches(this, Enchantments.SILK_TOUCH)) return;
		if (stack.isIn(ItemTags.WEAPON_ENCHANTABLE)) cir.setReturnValue(true);
	}
}
