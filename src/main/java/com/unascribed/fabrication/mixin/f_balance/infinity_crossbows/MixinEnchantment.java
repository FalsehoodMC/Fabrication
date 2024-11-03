package com.unascribed.fabrication.mixin.f_balance.infinity_crossbows;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.mixin.z_combined.enchantments.AccessorEnchantmentDefinition;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.component.ComponentMap;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Enchantment.class)
@EligibleIf(anyConfigAvailable={"*.infinity_crossbows", "*.infinity_crossbows_modded"})
public abstract class MixinEnchantment {
	@FabInject(at=@At("HEAD"), method="isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	private void isAcceptable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isAnyEnabled("*.infinity_crossbows") && EnchantmentHelperHelper.matches(this, Enchantments.INFINITY)) {
			if (FabConf.isEnabled("*.infinity_crossbows_modded") ? stack.getItem() instanceof CrossbowItem : stack.getItem() == Items.CROSSBOW) {
				cir.setReturnValue(true);
			}
		}
	}
}
