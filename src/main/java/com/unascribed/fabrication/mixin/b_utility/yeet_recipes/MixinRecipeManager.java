package com.unascribed.fabrication.mixin.b_utility.yeet_recipes;

import java.util.Map;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonElement;
import com.unascribed.fabrication.loaders.LoaderYeetRecipes;
import com.unascribed.fabrication.support.EligibleIf;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(RecipeManager.class)
@EligibleIf(configAvailable="*.yeet_recipes")
public class MixinRecipeManager {

	@Shadow
	private Map<Identifier, RecipeEntry<?>> recipesById;

	@Shadow
	private Multimap<RecipeType<?>, RecipeEntry<?>> recipesByType;

	@FabInject(at=@At("TAIL"), method="apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V")
	public void apply(Map<Identifier, JsonElement> map, ResourceManager rm, Profiler profiler, CallbackInfo ci) {
		if (FabConf.isEnabled("*.yeet_recipes")) {
			recipesById = Maps.filterKeys(recipesById, id -> !LoaderYeetRecipes.recipesToYeet.contains(id));
			recipesByType = Multimaps.filterEntries(recipesByType, entry -> !LoaderYeetRecipes.recipesToYeet.contains(entry.getValue().id()));
		}
	}

}
