package com.unascribed.fabrication.mixin.g_weird_tweaks.encroaching_emeralds;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultBiomeFeatures.class)
@EligibleIf(configAvailable="*.encroaching_emeralds")
public abstract class MixinDefaultBiomeFeatures {

	@FabInject(at=@At("TAIL"), method="addDefaultOres(Lnet/minecraft/world/biome/GenerationSettings$Builder;)V")
	private static void addEmeralds(GenerationSettings.Builder builder, CallbackInfo ci){
		if (FabConf.isEnabled("*.encroaching_emeralds")) DefaultBiomeFeatures.addEmeraldOre(builder);
	}
}
