package com.unascribed.fabrication.util.forgery_nonsense;

import net.minecraft.util.Identifier;

public class ForgeryIdentifier {
	//This exists because forgery is jank
	public static Identifier get(String s) {
		return Identifier.of(s);
	}
	public static Identifier get(String s1, String s2) {
		return Identifier.of(s1, s2);
	}

}
