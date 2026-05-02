package com.unascribed.fabrication.util.forgery_nonsense;

import net.minecraft.util.Pair;

public class ForgeryPair {
	//This exists because forgery is jank
	public static<A, B> Pair<A, B> get(A a, B b) {
		return new Pair<>(a, b);
	}

}
