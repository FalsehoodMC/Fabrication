package com.unascribed.fabrication.mixin.g_weird_tweaks.dimensional_tools;

import com.unascribed.fabrication.FabConf;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.features.FeatureDimensionalTools;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapelessRecipe;

@Mixin(ShapelessRecipe.class)
@EligibleIf(configAvailable="*.dimensional_tools")
public class MixinShapelessRecipe {

	// even though this signature is identical to the one in ShapedRecipe, a multi-target mixin
	// can't be used because the two methods obfuscate differently
	@FabInject(at=@At("RETURN"), method="craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;")
	public void craft(CraftingRecipeInput inv, RegistryWrapper.WrapperLookup wrapperLookup, CallbackInfoReturnable<ItemStack> cir) {
		if (!FabConf.isEnabled("*.dimensional_tools")) return;
		FeatureDimensionalTools.handleCraft(inv, cir.getReturnValue());
	}

}
