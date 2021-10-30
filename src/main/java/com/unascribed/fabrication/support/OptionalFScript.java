package com.unascribed.fabrication.support;

import com.mojang.brigadier.context.CommandContext;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.features.FeatureFabricationCommand;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.PredicateProvider;
import tf.ssf.sfort.script.ScriptParser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class OptionalFScript {
    public static Map<String, Object> predicateProviders = new HashMap<>();
    static {
        predicateProviders.put("tweaks.feather_falling_no_trample", Default.LIVING_ENTITY);
        predicateProviders.put("balance.food_always_edible", Default.PLAYER_ENTITY);
        predicateProviders.put("balance.disable_mending", Default.PLAYER_ENTITY);
        predicateProviders.put("balance.disable_elytra_boost", Default.PLAYER_ENTITY);
        predicateProviders.put("woina.instant_bow", Default.PLAYER_ENTITY);
        predicateProviders.put("woina.no_sprint", Default.LIVING_ENTITY);
    }

    public static<T> void setScript(CommandContext<? extends CommandSource> c, String configKey, String script){
        setScript(configKey, script, e -> FeatureFabricationCommand.sendFeedback(c, new LiteralText("Failed to set script for "+configKey+"\n"+e.getLocalizedMessage()), true));
    }
    public static<T> void setScript(String configKey, String script){
        setScript(configKey, script, e -> FabLog.error("Failed to set script for "+configKey, e));
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
            MixinConfigPredicates.predicates.put(configKey, predicate);
        }catch (Exception e){
            exceptionConsumer.accept(e);
        }
    }

}
