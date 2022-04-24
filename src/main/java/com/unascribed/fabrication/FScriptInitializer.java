package com.unascribed.fabrication;

import com.unascribed.fabrication.features.FeatureTaggablePlayers;
import com.unascribed.fabrication.support.TaggablePlayersProvider;
import net.fabricmc.api.ModInitializer;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.ExtendablePredicateProvider;
import tf.ssf.sfort.script.PredicateProvider;

public class FScriptInitializer implements ModInitializer {
	@Override
	public void onInitialize() {
		PredicateProvider<?> p = Default.getDefaultMap().get("SERVER_PLAYER_ENTITY");
		if (p instanceof ExtendablePredicateProvider) {
			try {
				((ExtendablePredicateProvider) p).addProvider(TaggablePlayersProvider.INSTANCE);
			} catch (Exception e) {
				FabLog.error("Failed to cast ServerPlayerEntity predicate provider", e);
			}
		}
		Default.PARAMETERS.addParameterSupplier("FabricationTaggablePlayersID", FeatureTaggablePlayers.validTags::keySet);
	}
}
