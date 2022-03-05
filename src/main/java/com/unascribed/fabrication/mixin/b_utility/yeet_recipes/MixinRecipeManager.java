package com.unascribed.fabrication.mixin.b_utility.yeet_recipes;

import java.util.Map;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonElement;
import com.unascribed.fabrication.loaders.LoaderYeetRecipes;
import com.unascribed.fabrication.support.EligibleIf;

import com.google.common.collect.Maps;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(RecipeManager.class)
@EligibleIf(configAvailable="*.yeet_recipes")
public class MixinRecipeManager {

	@Shadow
	private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;

	@Inject(at=@At("TAIL"), method="apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V")
	public void apply(Map<Identifier, JsonElement> map, ResourceManager rm, Profiler profiler, CallbackInfo ci) {
		recipes = Maps.transformValues(recipes, m -> Maps.filterKeys(m, k -> !FabConf.isEnabled("*.yeet_recipes") || !LoaderYeetRecipes.recipesToYeet.contains(k)));
	}

}
