package com.unascribed.fabrication.util;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;

public class EffectNeedsReplacing {
	public static boolean needsReplacing(PlayerEntity pe, RegistryEntry<StatusEffect> se) {
		return !pe.hasStatusEffect(se) || !pe.getStatusEffect(se).isAmbient() || pe.getStatusEffect(se).shouldShowIcon() || pe.getStatusEffect(se).shouldShowParticles();
	}
}
