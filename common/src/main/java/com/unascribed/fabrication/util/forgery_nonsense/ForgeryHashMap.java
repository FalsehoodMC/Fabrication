package com.unascribed.fabrication.util.forgery_nonsense;

import java.util.HashMap;

public class ForgeryHashMap {
	//This exists because forgery is jank
	public static<K, V> HashMap<K, V> get() {
		return new HashMap<>();
	}

}
