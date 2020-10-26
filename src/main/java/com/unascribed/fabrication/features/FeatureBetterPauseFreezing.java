package com.unascribed.fabrication.features;

import java.util.function.LongSupplier;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.screen.options.SkinOptionsScreen;
import net.minecraft.client.gui.screen.options.SoundOptionsScreen;
import net.minecraft.util.Util;

@EligibleIf(configEnabled="*.better_pause_freezing", envMatches=Env.CLIENT)
public class FeatureBetterPauseFreezing implements Feature {

	private static final ImmutableSet<Class<? extends Screen>> FREEZABLE_SCREENS = ImmutableSet.of(
			GameMenuScreen.class,
			OptionsScreen.class,
			SkinOptionsScreen.class,
			SoundOptionsScreen.class,
			AdvancementsScreen.class,
			OpenToLanScreen.class,
			ConfirmChatLinkScreen.class
	);
	
	private boolean applied = false;
	
	private LongSupplier originalClock;
	
	private long pausedAt = -1;
	private long offset = 0;
	
	@Override
	public void apply() {
		applied = true;
		MinecraftClient.getInstance().submit(() -> {
			if (!applied) return;
			originalClock = Util.nanoTimeSupplier;
			Util.nanoTimeSupplier = () -> {
				// because we apply drift to the clock, if this feature has ever been enabled it must
				// stay enabled or we will cause tick catchup
				if (applied) {
					if (MinecraftClient.getInstance().isPaused() && MinecraftClient.getInstance().getServer() != null && !MinecraftClient.getInstance().getServer().isRemote()
							&& MinecraftClient.getInstance().currentScreen != null && FREEZABLE_SCREENS.contains(MinecraftClient.getInstance().currentScreen.getClass())) {
						if (pausedAt == -1) {
							pausedAt = originalClock.getAsLong();
						}
						return pausedAt;
					} else {
						if (pausedAt != -1) {
							offset += originalClock.getAsLong()-pausedAt;
							pausedAt = -1;
						}
					}
				}
				return originalClock.getAsLong()-offset;
			};
		});
	}

	@Override
	public boolean undo() {
		applied = false;
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.better_pause_freezing";
	}

}
