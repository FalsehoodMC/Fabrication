package com.unascribed.fabrication.util;

import java.util.Map;

import com.google.common.collect.Maps;

public class TransientThreadStorage {

	public static final class Key<T> {}

	private static final ThreadLocal<TransientThreadStorage> LOCAL = ThreadLocal.withInitial(TransientThreadStorage::new);

	private final Map<Key<?>, Object> map = Maps.newHashMap();

	public <T> T get(Key<T> key) {
		return (T)map.get(key);
	}

	public void remove(Key<?> key) {
		map.remove(key);
	}

	public <T> void put(Key<T> key, T val) {
		map.put(key, val);
	}

	public static TransientThreadStorage get() {
		return LOCAL.get();
	}

}
