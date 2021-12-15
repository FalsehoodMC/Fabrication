package com.unascribed.fabrication.mixin.g_weird_tweaks.encroaching_emeralds;

import com.unascribed.fabrication.interfaces.GenerationSettingsBuilderContains;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.OrePlacedFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultBiomeFeatures.class)
@EligibleIf(configAvailable="*.encroaching_emeralds")
public abstract class MixinDefaultBiomeFeatures {

	@Inject(at=@At("TAIL"), method="addDefaultOres(Lnet/minecraft/world/biome/GenerationSettings$Builder;)V")
	private static void addEmeralds(GenerationSettings.Builder builder, CallbackInfo ci){
		if (MixinConfigPlugin.isEnabled("*.encroaching_emeralds")) DefaultBiomeFeatures.addEmeraldOre(builder);
	}
	@Inject(at=@At("HEAD"), method="addEmeraldOre(Lnet/minecraft/world/biome/GenerationSettings$Builder;)V", cancellable=true)
	private static void removeEmeralds(GenerationSettings.Builder builder, CallbackInfo ci){
		if (builder instanceof GenerationSettingsBuilderContains && ((GenerationSettingsBuilderContains)builder).fabrication$builderContains(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_EMERALD))
			ci.cancel();
	}
}
