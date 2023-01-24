package com.unascribed.fabrication.util.forgery_nonsense;

import java.util.ArrayList;

public class ForgeryArrayList {
	//This exists because forgery is jank
	public static<V> ArrayList<V> get() {
		return new ArrayList<>();
	}

}
