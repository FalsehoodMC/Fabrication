package com.unascribed.fabrication.support;

import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.features.FeatureFabricationCommand;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.ScriptParser;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OptionalFScript {
    public static Map<String, PredicateProvider<?>> predicateProviders;
    static {
        predicateProviders = FeaturesFile.getAll().entrySet().stream().filter(f -> f.getValue().fscript != null).collect(Collectors.toMap(Map.Entry::getKey, v -> Default.getDefaultMap().get(v.getValue().fscript)));
    }

    public static<T> void setScript(CommandContext<? extends CommandSource> c, String configKey, String script){
        setScript(configKey, script, e -> FeatureFabricationCommand.sendFeedback(c, new LiteralText("Failed to set script for "+configKey+"\n"+e.getLocalizedMessage()), true));
    }
    public static<T> void setScript(String configKey, String script){
        setScript(configKey, script, e -> FabLog.error("Failed to set script for "+configKey, e));
    }
    public static void restoreDefault(String configKey){
        configKey = MixinConfigPlugin.remap(configKey);
        if (ConfigPredicates.predicateDefaults.containsKey(configKey)) ConfigPredicates.predicates.put(configKey, ConfigPredicates.predicateDefaults.get(configKey));
        else ConfigPredicates.predicates.remove(configKey);
    }
    private static<T> void setScript(String configKey, String script, Consumer<Exception> exceptionConsumer){
        configKey = MixinConfigPlugin.remap(configKey);
        try {
            PredicateProvider<T> predicateProvider = (PredicateProvider<T>) predicateProviders.get(configKey);
            if (predicateProvider == null) {
                exceptionConsumer.accept(new Exception("No predicate provider exists for specified key"));
                return;
            }
            Predicate<T> predicate = new ScriptParser<>(predicateProvider).parse(script);
            if (predicate == null ) {
                exceptionConsumer.accept(new Exception("FScript returned null, likely because an invalid script was given"));
                return;
            }
            ConfigPredicates.predicates.put(configKey, predicate);
        }catch (Exception e){
            exceptionConsumer.accept(e);
        }
    }

}
