package com.unascribed.fabrication.mixin.c_tweaks.long_levelup_sound_at_30;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

@Mixin(ClientWorld.class)
@EligibleIf(configEnabled="*.long_levelup_sound_at_30", envMatches=Env.CLIENT)
public class MixinClientWorld {

	@Inject(at=@At("HEAD"), method="playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V", cancellable=true)
	public void playSound(double x, double y, double z, SoundEvent event, SoundCategory category, float pitch, float volume, boolean useDistance, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.long_levelup_sound_at_30")) return;
		if (event == SoundEvents.ENTITY_PLAYER_LEVELUP && category == SoundCategory.PLAYERS) {
			int lvl = MinecraftClient.getInstance().player.experienceLevel;
			if (lvl >= 25 && lvl < 30 && MinecraftClient.getInstance().player.squaredDistanceTo(x, y, z) < 0.05) {
				((ClientWorld)(Object)this).playSound(x, y, z, FabricationMod.LEVELUP_LONG, category, pitch, volume, useDistance);
				ci.cancel();
			}
		}
	}

}
