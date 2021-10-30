package com.unascribed.fabrication.support;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class MixinConfigPredicates {

    public static Map<String, Object> predicates = new HashMap<>();

    public static<T> boolean shouldRun(String configKey, T test) {
        return shouldRun(configKey, test, true);
    }

    public static<T> boolean shouldRun(String configKey, T test, boolean ifMissingReturn) {
        configKey = MixinConfigPlugin.remap(configKey);
        Predicate<T> predicate;
        try{
             predicate= (Predicate<T>) predicates.get(configKey);
            if (predicate == null) return ifMissingReturn;
        }catch (Exception e){
            return ifMissingReturn;
        }
        return predicate.test(test);
    }
}
