package com.unascribed.fabrication.logic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class PingPrivacy {

	// E is more likely
	private static final String vowels = "aeeeiou";
	private static final String consonants = "bcdfghjklmprstvwy ";
	private static final String rareConsonants = "xqz";
	private static final String numbers = "0123456789";

	public static void generateBelievableUsername(Random r, StringBuilder sb) {
		int len = r.nextInt(13)+3;
		boolean spammingNumbers = false;
		sb.setLength(0);
		while (sb.length() < len) {
			if ((sb.length() == 0 || sb.length() == len-1) && r.nextInt(6) == 0) {
				sb.append("_");
				continue;
			}
			if (spammingNumbers || r.nextInt(8) == 0) {
				spammingNumbers = true;
				sb.append(pick(r, numbers));
			} else {
				char nextConsonant;
				if (r.nextInt(20) == 0) {
					nextConsonant = pick(r, rareConsonants);
				} else {
					nextConsonant = pick(r, consonants);
				}
				if ((sb.length() == 0 && r.nextBoolean()) || r.nextInt(12) == 0) {
					nextConsonant = Character.toUpperCase(nextConsonant);
				}
				if (nextConsonant != ' ') {
					sb.append(nextConsonant);
					if (nextConsonant != 'y' && r.nextInt(10) == 0) {
						sb.append('y');
					}
				}
				sb.append(pick(r, vowels));
			}
		}
	}

	private static char pick(Random r, String s) {
		return s.charAt(r.nextInt(s.length()));
	}

	private static final Set<InetAddress> EVIL_ADDRESSES = ImmutableSet.of(
			// NameMC - they constantly ping servers without permission and collect data on players with no opt-out
			constantAddress("51.222.110.150")
			);

	private static InetAddress constantAddress(String addr) {
		try {
			return InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			throw new AssertionError(e);
		}
	}

	public static boolean isEvil(InetAddress addr) {
		return EVIL_ADDRESSES.contains(addr);
	}

}
