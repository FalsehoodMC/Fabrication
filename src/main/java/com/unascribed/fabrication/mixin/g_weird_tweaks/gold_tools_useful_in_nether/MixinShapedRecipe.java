package com.unascribed.fabrication.mixin.g_weird_tweaks.gold_tools_useful_in_nether;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.features.FeatureGoldToolsUsefulInNether;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.block.Block;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Language;

@Mixin(ShapedRecipe.class)
@EligibleIf(configEnabled="*.gold_tools_useful_in_nether")
public class MixinShapedRecipe {

	@Inject(at=@At("RETURN"), method="craft(Lnet/minecraft/inventory/CraftingInventory;)Lnet/minecraft/item/ItemStack;")
	public void craft(CraftingInventory inv, CallbackInfoReturnable<ItemStack> ci) {
		if (!RuntimeChecks.check("*.gold_tools_useful_in_nether")) return;
		ItemStack stack = ci.getReturnValue();
		if (stack.getItem().isIn(FeatureGoldToolsUsefulInNether.POSSIBLY_FUNGAL_TOOLS)) {
			boolean fungal = false;
			for (int i = 0; i < inv.size(); i++) {
				ItemStack ing = inv.getStack(i);
				if (ing.getItem() instanceof BlockItem) {
					Block b = ((BlockItem)ing.getItem()).getBlock();
					if (b.isIn(BlockTags.PLANKS)) {
						if (!b.isIn(BlockTags.NON_FLAMMABLE_WOOD)) {
							fungal = false;
							break;
						} else {
							fungal = true;
						}
					}
				}
			}
			if (fungal) {
				stack.setCustomName(new LiteralText("§f"+Language.getInstance().get(stack.getTranslationKey()).replace("Wooden", "Fungal")));
				stack.getTag().putBoolean("fabrication:ActLikeGold", true);
			}
		}
	}
	
}
