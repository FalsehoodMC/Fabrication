package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;

import java.util.function.Predicate;

@EligibleIf(configAvailable="*.invisible_to_mobs")
public class FeatureInvisibleToMobs implements Feature {

	private Predicate<Entity> originalUntargetablePredicate;

	@Override
	public void apply() {
		originalUntargetablePredicate = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR;
		Predicate<PlayerEntity> predicate = ConfigPredicates.getFinalPredicate("*.invisible_to_mobs");
		amendUntargetablePredicate(e -> {
			if (e instanceof PlayerEntity && predicate.test((PlayerEntity)e)) return false;
			return originalUntargetablePredicate.test(e);
		});
	}

	private void amendUntargetablePredicate(Predicate<Entity> p) {
		EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR = p;
	}

	@Override
	public boolean undo() {
		if (originalUntargetablePredicate != null) {
			amendUntargetablePredicate(originalUntargetablePredicate);
		}
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.invisible_to_mobs";
	}



}
