package com.unascribed.fabrication.features;

import java.util.function.Predicate;

import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.logic.PlayerTag;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;

@EligibleIf(configEnabled="*.taggable_players")
public class FeatureTaggablePlayers implements Feature {

	private Predicate<Entity> originalUntargetablePredicate;

	@Override
	public void apply() {
		originalUntargetablePredicate = EntityPredicates.EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL;
		amendUntargetablePredicate(e -> {
			if (e instanceof TaggablePlayer && ((TaggablePlayer)e).fabrication$hasTag(PlayerTag.INVISIBLE_TO_MOBS)) return false;
			return originalUntargetablePredicate.test(e);
		});
	}

	private void amendUntargetablePredicate(Predicate<Entity> p) {
		EntityPredicates.EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL = p;
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
		return "*.taggable_players";
	}



}