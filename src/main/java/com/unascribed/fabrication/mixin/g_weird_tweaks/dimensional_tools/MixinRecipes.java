package com.unascribed.fabrication.mixin.g_weird_tweaks.dimensional_tools;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.loaders.LoaderDimensionalTools;
import com.unascribed.fabrication.loaders.LoaderDimensionalTools.MohsIdentifier;
import com.unascribed.fabrication.loaders.LoaderDimensionalTools.NameSubstitution;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Language;

@Mixin({ShapedRecipe.class, ShapelessRecipe.class})
@EligibleIf(configEnabled="*.dimensional_tools")
public class MixinRecipes {

	@Inject(at=@At("RETURN"), method="craft(Lnet/minecraft/inventory/CraftingInventory;)Lnet/minecraft/item/ItemStack;")
	public void craft(CraftingInventory inv, CallbackInfoReturnable<ItemStack> ci) {
		if (!RuntimeChecks.check("*.dimensional_tools")) return;
		ItemStack stack = ci.getReturnValue();
		if (LoaderDimensionalTools.isSubstitutable(stack.getItem())) {
			int count = 0;
			Multiset<MohsIdentifier> dimensions = null;
			for (int i = 0; i < inv.size(); i++) {
				ItemStack ing = inv.getStack(i);
				if (!ing.isEmpty()) {
					Set<MohsIdentifier> assoc = LoaderDimensionalTools.getAssociatedDimensionsForIngredient(ing);
					if (assoc.isEmpty()) return;
					if (assoc.contains(null)) continue;
					count++;
					if (dimensions == null) dimensions = HashMultiset.create();
					dimensions.addAll(assoc);
				}
			}
			if (dimensions != null) {
				Set<MohsIdentifier> finalDimensions = Sets.newHashSet();
				for (Multiset.Entry<MohsIdentifier> en : dimensions.entrySet()) {
					if (en.getCount() == count) {
						finalDimensions.add(en.getElement());
					}
				}
				if (!finalDimensions.isEmpty()) {
					String s = Language.getInstance().get(stack.getTranslationKey());
					for (NameSubstitution sub : LoaderDimensionalTools.nameSubstitutions) {
						if (finalDimensions.contains(new MohsIdentifier(true, sub.dimId)) ||
								finalDimensions.contains(new MohsIdentifier(false, sub.dimId))) {
							s = s.replace(sub.find, sub.replace);
						}
					}
					stack.setCustomName(new LiteralText("§f"+s));
					ListTag li = new ListTag();
					for (MohsIdentifier dim : finalDimensions) {
						li.add(StringTag.of(dim.toString()));
					}
					stack.getTag().put("fabrication:HonoraryDimensions", li);
				}
			}
		}
	}
	
}
