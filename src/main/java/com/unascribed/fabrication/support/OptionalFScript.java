package com.unascribed.fabrication.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.features.FeatureFabricationCommand;
import com.unascribed.fabrication.loaders.LoaderFScript;

import net.minecraft.command.CommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.StitchedPredicateProvider;

public class OptionalFScript {
	public static ImmutableMap<String, PredicateProvider<?>> predicateProviders;
	static {
		Map<String, PredicateProvider<?>> providers = new HashMap<>();
		for (Map.Entry<String, FeaturesFile.FeatureEntry> entry : FeaturesFile.getAll().entrySet()) {
			FeaturesFile.FeatureEntry f = entry.getValue();
			if (f.fscript == null) continue;
			PredicateProvider<?> p = Default.getDefaultMap().get(f.fscript);
			if (!f.extraFscript.isEmpty()) {
				p = new StitchedPredicateProvider(p);
				for (Map.Entry<String, Map.Entry<String, String>> extraEntry : f.extraFscript.entrySet()){
					((StitchedPredicateProvider)p).addEmbed(Default.getDefaultMap().get(extraEntry.getKey()), extraEntry.getValue().getKey(), extraEntry.getValue().getValue());
				}
			}
			providers.put(entry.getKey(), p);
		}
		predicateProviders = ImmutableMap.copyOf(providers);
	}

	public static void set(CommandContext<? extends CommandSource> c, String configKey, String script){
		set(configKey, script, e -> FeatureFabricationCommand.sendFeedback(c, Text.literal("Failed to set script for " + configKey + "\n" + e.getLocalizedMessage()), true));
	}
	public static boolean set(String configKey, String script, ServerPlayerEntity spe){
		Optional<Exception> err = setScript(configKey, script);
		err.ifPresent(e -> spe.sendMessage(Text.literal("Failed to set script for "+configKey+"\n"+e.getLocalizedMessage())));
		return !err.isPresent();
	}
	public static void set(String configKey, String script, Consumer<Exception> errReporter){
		setScript(configKey, script).ifPresent(errReporter);
	}
	public static void restoreDefault(String configKey){
		configKey = FabConf.remap(configKey);
		ConfigPredicates.remove(configKey, 2);
		LoaderFScript.put(configKey, null);
	}
	public static void reload(){
		for (Map.Entry<String, String> entry : LoaderFScript.getMap().entrySet()){
			setScript(entry.getKey(), entry.getValue());
		}
	}
	private static Optional<Exception> setScript(String configKey, String script){
		configKey = FabConf.remap(configKey);
		try {
			PredicateProvider<?> predicateProvider = predicateProviders.get(configKey);
			if (predicateProvider == null) return Optional.of(new Exception("No predicate provider exists for specified key"));
			Predicate<?> predicate = predicateProvider.parse(script);
			if (predicate == null ) return Optional.of(new Exception("FScript returned null, likely because an invalid script was given"));
			ConfigPredicates.put(configKey, predicate, 2);
			LoaderFScript.put(configKey, script);
		}catch (Exception e){
			FabLog.error("Failed to set script for "+configKey, e);
			return Optional.of(e);
		}
		return Optional.empty();
	}

}
