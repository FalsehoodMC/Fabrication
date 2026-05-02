package com.unascribed.fabrication.util;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class FakePlayerChecker {

	private static final Map<Class<? extends PlayerEntity>, Boolean> cache = new HashMap<>();

	public static boolean isFakePlayer(PlayerEntity o) {
		Class<? extends PlayerEntity> c = o.getClass();
		Boolean res = cache.get(c);
		if (res == null) {
			res = false;
			Class<?> cursor = c;
			while (cursor != null) {
				if (cursor.getName().contains("Fake")) {
					res = true;
					break;
				}
				cursor = cursor.getSuperclass();
			}
			cache.put(c, res);
		}
		return res;
	}

}


