package com.unascribed.fabrication.support;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.features.FeatureTaggablePlayers;
import net.minecraft.server.network.ServerPlayerEntity;
import tf.ssf.sfort.script.Help;
import tf.ssf.sfort.script.PredicateProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class TaggablePlayersProvider implements PredicateProvider<ServerPlayerEntity>, Help {
	public static final TaggablePlayersProvider INSTANCE = new TaggablePlayersProvider();
	public final Map<String, String> help = Collections.singletonMap("fabrication$tag fab$tag:FabricationTaggablePlayersID", "Require player to have the specified taggable players feature active");

	@Override
	public Map<String, String> getHelp() {
		return help;
	}

	@Override
	public Predicate<ServerPlayerEntity> getPredicate(String key, String arg, Set<String> dejavu) {
		switch (key) {
			case "fabrication$tag":
			case "fab$tag":
				String tag = FabConf.remap(arg);
				String reKey = tag.substring(tag.lastIndexOf('.')+1);
				if (FeatureTaggablePlayers.validTags.containsKey(tag)) {
					return player -> {
						Integer i = FeatureTaggablePlayers.activeTags.get(tag);
						if (i == null) return FabConf.isEnabled(tag);
						return FabConf.isEnabled(tag) && FeatureTaggablePlayers.getPredicate(reKey, i).test(player);
					};
				}
				break;
		}
		return null;
	}
}
