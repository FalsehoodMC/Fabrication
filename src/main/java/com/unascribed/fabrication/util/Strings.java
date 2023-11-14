package com.unascribed.fabrication.util;

public class Strings {
	public static String capitalizeIdenfier(String str) {
		if (str == null || str.length() == 0) return str;
		char[] buffer = str.toCharArray();
		char ch = buffer[0];
		if (ch >= 'a' && ch <= 'z') {
			buffer[0] ^= 0x20;
		}
		for (int i=1; i<buffer.length; i++) {
			ch = buffer[i];
			if (ch == '_') {
				buffer[i] = ' ';
				if (++i<buffer.length) {
					ch = buffer[i];
					if (ch >= 'a' && ch <= 'z') {
						buffer[i] ^= 0x20;
					}
				}
			}
		}
		return new String(buffer);
	}
}
