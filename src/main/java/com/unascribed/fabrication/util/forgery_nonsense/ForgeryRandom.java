package com.unascribed.fabrication.util.forgery_nonsense;

import java.util.Random;

public class ForgeryRandom {
	//This exists because forgery is jank
	public static Random get() {
		return new Random();
	}
}
