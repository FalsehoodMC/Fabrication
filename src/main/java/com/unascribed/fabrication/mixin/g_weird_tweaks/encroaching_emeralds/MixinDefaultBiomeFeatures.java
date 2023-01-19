package com.unascribed.fabrication.mixin.g_weird_tweaks.encroaching_emeralds;

import com.unascribed.fabrication.interfaces.GenerationSettingsAddEmeralds;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.FabConf;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultBiomeFeatures.class)
@EligibleIf(configAvailable="*.encroaching_emeralds")
public abstract class MixinDefaultBiomeFeatures {

	@FabInject(at=@At("HEAD"), method="addDefaultOres(Lnet/minecraft/world/biome/GenerationSettings$LookupBackedBuilder;Z)V")
	private static void addEmeralds(GenerationSettings.LookupBackedBuilder builder, boolean bool, CallbackInfo ci){
		if (!(FabConf.isEnabled("*.encroaching_emeralds") && builder instanceof GenerationSettingsAddEmeralds)) return;
		((GenerationSettingsAddEmeralds)builder).fabrication$addEmeralds();
	}
}
