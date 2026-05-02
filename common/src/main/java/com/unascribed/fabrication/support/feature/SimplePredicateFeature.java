package com.unascribed.fabrication.support.feature;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.Feature;

import java.util.function.Predicate;

public abstract class SimplePredicateFeature implements Feature {
	public final String key;
	public final Predicate<?> predicate;

	public SimplePredicateFeature(String key, Predicate<?> predicate) {
		this.key = key;
		this.predicate = predicate;
	}

	@Override
	public void apply() {
		ConfigPredicates.put(key, predicate);
	}

	@Override
	public boolean undo() {
		ConfigPredicates.remove(key);
		return true;
	}

}
