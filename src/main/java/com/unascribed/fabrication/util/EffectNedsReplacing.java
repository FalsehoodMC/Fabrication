package com.unascribed.fabrication.util;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;

public class EffectNedsReplacing {
	public static boolean needsReplacing(PlayerEntity pe, StatusEffect se) {
		return !pe.hasStatusEffect(se) || !pe.getStatusEffect(se).isAmbient() || pe.getStatusEffect(se).shouldShowIcon() || pe.getStatusEffect(se).shouldShowParticles();
	}
}
