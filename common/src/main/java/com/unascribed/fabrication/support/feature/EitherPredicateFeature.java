package com.unascribed.fabrication.support.feature;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.Feature;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public abstract class EitherPredicateFeature<T> implements Feature {
	public final String key;
	public final Predicate<T> predicate;
	public static Map<String, Map<String, Object>> builder = new HashMap<>();

	public EitherPredicateFeature(String key, Predicate<T> predicate) {
		this.key = key;
		this.predicate = predicate;
		builder.putIfAbsent(key, new HashMap<>());
	}

	@Override
	public void apply() {
		builder.get(key).put(getConfigKey(), predicate);
		rebuild();
	}
	public void rebuild(){
		if (builder.get(key).isEmpty()) {
			ConfigPredicates.remove(key);
		} else {
			ConfigPredicates.put(key, builder.get(key).values().stream().map(p->(Predicate<T>)p).reduce((r, p)->v->r.test(v)||p.test(v)).get());
		}
	}

	@Override
	public boolean undo() {
		builder.get(key).remove(getConfigKey());
		rebuild();
		return true;
	}

}
