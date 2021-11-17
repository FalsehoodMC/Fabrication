package com.unascribed.fabrication.support;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.features.FeatureFabricationCommand;
import com.unascribed.fabrication.loaders.LoaderFScript;

import net.minecraft.command.CommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.ScriptParser;

public class OptionalFScript {
	public static Map<String, PredicateProvider<?>> predicateProviders;
	static {
		predicateProviders = FeaturesFile.getAll().entrySet().stream().filter(f -> f.getValue().fscript != null).collect(Collectors.toMap(Map.Entry::getKey, v -> Default.getDefaultMap().get(v.getValue().fscript)));
	}

	public static void set(CommandContext<? extends CommandSource> c, String configKey, String script){
		setScript(configKey, script).ifPresent(e -> FeatureFabricationCommand.sendFeedback(c, new LiteralText("Failed to set script for "+configKey+"\n"+e.getLocalizedMessage()), true));
	}
	public static boolean set(String configKey, String script, ServerPlayerEntity spe){
		Optional<Exception> err = setScript(configKey, script);
		err.ifPresent(e -> spe.sendSystemMessage(new LiteralText("Failed to set script for "+configKey+"\n"+e.getLocalizedMessage()), Util.NIL_UUID));
		return !err.isPresent();
	}
	public static void set(String configKey, String script){
		setScript(configKey, script).ifPresent(e -> FabLog.error("Failed to set script for "+configKey, e));
	}
	public static void restoreDefault(String configKey){
		configKey = MixinConfigPlugin.remap(configKey);
		ConfigPredicates.remove(configKey, 2);
		LoaderFScript.put(configKey, null);
	}
	public static void reload(){
		for (Map.Entry<String, String> entry : LoaderFScript.getMap().entrySet()){
			set(entry.getKey(), entry.getValue());
		}
	}
	private static Optional<Exception> setScript(String configKey, String script){
		configKey = MixinConfigPlugin.remap(configKey);
		try {
			PredicateProvider<?> predicateProvider = predicateProviders.get(configKey);
			if (predicateProvider == null) return Optional.of(new Exception("No predicate provider exists for specified key"));
			Predicate<?> predicate = new ScriptParser<>(predicateProvider).parse(script);
			if (predicate == null ) return Optional.of(new Exception("FScript returned null, likely because an invalid script was given"));
			ConfigPredicates.put(configKey, predicate, 2);
			LoaderFScript.put(configKey, script);
		}catch (Exception e){
			return Optional.of(e);
		}
		return Optional.empty();
	}

}