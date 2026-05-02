package com.unascribed.fabrication.mixin.e_mechanics.fire_resistance_two;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

@Mixin(BrewingRecipeRegistry.class)
@EligibleIf(configAvailable="*.fire_resistance_two")
public abstract class MixinBrewingRecipeRegistry {

	@FabInject(method="hasRecipe(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", at=@At("HEAD"), cancellable=true)
	private static void fabrication$fireResistTwoRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.fire_resistance_two")) return;
		if (ingredient.isOf(Items.GLOWSTONE_DUST) && PotionUtil.getPotion(input) == Potions.FIRE_RESISTANCE) {
			cir.setReturnValue(true);
		}
	}
	@FabInject(method="craft(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", at=@At("HEAD"), cancellable=true)
	private static void fabrication$fireResistTwoCraft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
		if (!FabConf.isEnabled("*.fire_resistance_two")) return;
		if (ingredient.isOf(Items.GLOWSTONE_DUST) && PotionUtil.getPotion(input) == Potions.FIRE_RESISTANCE) {
			ItemStack ret = Items.POTION.getDefaultStack();
			PotionUtil.setCustomPotionEffects(ret, Collections.singleton(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3600, 1)));
			ret.setCustomName(Text.of("Potion of Lava Resistance"));
			cir.setReturnValue(ret);
		}
	}
}
