package com.unascribed.fabrication.features;

import com.google.common.collect.ImmutableSet;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.loaders.LoaderTaggablePlayers;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@EligibleIf(configAvailable="*.taggable_players")
public class FeatureTaggablePlayers implements Feature {
	public static final ImmutableSet<String> validTags;

	static {
		Set<String> tags = new HashSet<>();
		FeaturesFile.getAll().forEach((key, val)->{
			if ("PLAYER_ENTITY".equals(val.fscript)) tags.add("*"+key.substring(key.lastIndexOf('.')));
		});
		validTags = ImmutableSet.copyOf(tags);
	}

	private Predicate<Entity> originalUntargetablePredicate;
	public static void add(String key) {
		ConfigPredicates.put(key, pe -> pe instanceof TaggablePlayer && ((TaggablePlayer) pe).fabrication$hasTag("*"+key.substring(key.lastIndexOf('.'))), 1);
		LoaderTaggablePlayers.instance.set(key, "true");
	}
	public static void remove(String key) {
		ConfigPredicates.remove(key, 1);
		LoaderTaggablePlayers.instance.remove(key);
	}
	@Override
	public void apply() {
		for (String key : LoaderTaggablePlayers.activeTags) {
			ConfigPredicates.put(key, pe -> pe instanceof TaggablePlayer && ((TaggablePlayer) pe).fabrication$hasTag("*"+key.substring(key.lastIndexOf('.'))), 1);
		}
		originalUntargetablePredicate = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR;
		amendUntargetablePredicate(e -> {
			if (e instanceof PlayerEntity && ConfigPredicates.shouldRun("*.invisible_to_mobs", (PlayerEntity)e)) return false;
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
		return "*.taggable_players";
	}



}
