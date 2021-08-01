package com.unascribed.fabrication.util;

import java.util.TreeMap;

import com.google.common.collect.Maps;

public class RomanNumeral {

	// inspired by https://stackoverflow.com/a/19759564
	private final static TreeMap<Integer, String> map = Maps.newTreeMap();
	static {
		map.put(1000, "M");
		map.put(900, "CM");
		map.put(500, "D");
		map.put(400, "CD");
		map.put(100, "C");
		map.put(90, "XC");
		map.put(50, "L");
		map.put(40, "XL");
		map.put(10, "X");
		map.put(9, "IX");
		map.put(5, "V");
		map.put(4, "IV");
		map.put(1, "I");
	}

	public static String format(int n) {
		if (n == 0) return "N";
		if (n > 1000000) return "∞";
		if (n > 10000) return Integer.toString(n);
		StringBuilder sb = new StringBuilder();
		while (n > 0) {
			int i = map.floorKey(n);
			sb.append(map.get(i));
			n -= i;
		}
		return sb.toString();
	}

}
