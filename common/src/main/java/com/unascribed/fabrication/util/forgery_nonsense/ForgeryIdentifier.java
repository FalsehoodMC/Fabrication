package com.unascribed.fabrication.util.forgery_nonsense;

import net.minecraft.util.Identifier;

public class ForgeryIdentifier {
	//This exists because forgery is jank
	public static Identifier get(String s) {
		return new Identifier(s);
	}
	public static Identifier get(String s1, String s2) {
		return new Identifier(s1, s2);
	}

}
