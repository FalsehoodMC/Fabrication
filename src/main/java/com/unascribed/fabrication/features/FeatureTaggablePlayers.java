package com.unascribed.fabrication.features;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.PlayerTag;
import com.unascribed.fabrication.interfaces.TaggablePlayer;
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
		Field field = FabricationMod.snagField(EntityPredicates.class, "field_22280", "EXCEPT_CREATIVE_SPECTATOR_OR_PEACEFUL");
		field.setAccessible(true);
		FieldUtils.removeFinalModifier(field);
		try {
			field.set(null, p);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
;