package com.unascribed.fabrication.mixin.g_weird_tweaks.dimensional_tools;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.DimensionalTools;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapelessRecipe;

@Mixin(ShapelessRecipe.class)
@EligibleIf(configAvailable="*.dimensional_tools")
public class MixinShapelessRecipe {

	// even though this signature is identical to the one in ShapedRecipe, a multi-target mixin
	// can't be used because the two methods obfuscate differently
	@Inject(at=@At("RETURN"), method="craft(Lnet/minecraft/inventory/CraftingInventory;)Lnet/minecraft/item/ItemStack;")
	public void craft(CraftingInventory inv, CallbackInfoReturnable<ItemStack> ci) {
		if (!MixinConfigPlugin.isEnabled("*.dimensional_tools")) return;
		DimensionalTools.handleCraft(inv, ci.getReturnValue());
	}

}
