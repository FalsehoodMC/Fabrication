package com.unascribed.fabrication.util;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.QDIni;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;

public class ParsedTime {
	public static class Unset extends ParsedTime {
		public static final Unset NORMAL = new Unset(6000, false);
		private Unset(int timeInTicks, boolean priority) {
			super(6000, priority);
		}
	}
	public static class Forever extends ParsedTime {
		private Forever(int timeInTicks, boolean priority) {
			super(Integer.MAX_VALUE, priority);
		}
	}
	public static class Invincible extends ParsedTime {
		private Invincible(int timeInTicks, boolean priority) {
			super(Integer.MAX_VALUE, priority);
		}
	}
	public static class Instant extends ParsedTime {
		private Instant(int timeInTicks, boolean priority) {
			super(0, priority);
		}
	}

	private static final int SECOND_IN_TICKS = 20;
	private static final int MINUTE_IN_TICKS = SECOND_IN_TICKS*60;
	private static final int HOUR_IN_TICKS = MINUTE_IN_TICKS*60;
	private static final Map<BiFunction<Integer, Boolean, ParsedTime>, Map<Integer, Map<Boolean, ParsedTime>>> cache = new HashMap<>();
	public final int timeInTicks;
	public final boolean priority;

	private ParsedTime(int timeInTicks, boolean priority) {
		super();
		this.timeInTicks = timeInTicks;
		this.priority = priority;
	}

	public boolean overshadows(ParsedTime that) {
		if (this == that) return false;
		if (this instanceof Unset) return false;
		if (that instanceof Unset) return true;
		if (that.priority && !this.priority) return false;
		if (this.priority && !that.priority) return true;
		if (this instanceof Invincible && that instanceof Forever) return true;
		return this.timeInTicks > that.timeInTicks;
	}

	@Override
	public String toString() {
		String s;
		if (this instanceof Unset) {
			s = "unset";
		} else if (this instanceof Forever) {
			s = "forever";
		} else if (this instanceof Invincible) {
			s = "invincible";
		} else if (this instanceof Instant) {
			s = "instantly";
		} else if (timeInTicks % HOUR_IN_TICKS == 0) {
			s = (timeInTicks/HOUR_IN_TICKS)+"h";
		} else if (timeInTicks % MINUTE_IN_TICKS == 0) {
			s = (timeInTicks/MINUTE_IN_TICKS)+"m";
		} else if (timeInTicks % SECOND_IN_TICKS == 0) {
			s = (timeInTicks/SECOND_IN_TICKS)+"s";
		} else {
			s = timeInTicks+"t";
		}
		return s+(priority ? "!" : "");
	}

	public static ParsedTime getFrom(QDIni cfg, String k) {
		String v = cfg.get(k).orElse("");
		try {
			return parse(v);
		} catch (IllegalArgumentException e) {
			FabLog.warn(k+" must be one of unset, forever, f, invincible, invulnerable, i, instantly, or a timespec like 30s (got "+v+") at "+cfg.getBlame(k));
			return cached(Unset::new, 6000, false);
		}
	}

	public static ParsedTime parse(String time) {
		Preconditions.checkNotNull(time);
		if (time.isEmpty()) {
			throw new IllegalArgumentException("Timespec cannot be blank");
		}
		boolean priority = false;
		if (time.endsWith("!")) {
			priority = true;
			time = time.substring(0, time.length()-1);
		}
		switch (time) {
			case "unset":
				return cached(Unset::new, 6000, priority);
			case "forever": case "f":
				return cached(Forever::new, Integer.MAX_VALUE, priority);
			case "invincible": case "invulnerable": case "i":
				return cached(Invincible::new, Integer.MAX_VALUE, priority);
			case "instantly": case "0":
				return cached(Instant::new, 0, priority);
		}
		int multiplier;
		char qualifier = time.charAt(time.length()-1);
		String timeNumPart = time.substring(0, time.length()-1);
		if (!CharMatcher.digit().matchesAllOf(timeNumPart)) {
			throw new IllegalArgumentException("Bad timespec "+time);
		}
		switch (qualifier) {
			case 't': multiplier = 1; break;
			case 's': multiplier = SECOND_IN_TICKS; break;
			case 'm': multiplier = MINUTE_IN_TICKS; break;
			case 'h': multiplier = HOUR_IN_TICKS; break;
			default: throw new IllegalArgumentException("Unknown qualifier "+qualifier+" for time value "+time);
		}
		return cached(ParsedTime::new, new BigDecimal(timeNumPart).multiply(new BigDecimal(multiplier)).intValueExact(), priority);
	}
	private static ParsedTime cached(BiFunction<Integer, Boolean, ParsedTime> constructor, int time, boolean priority) {
		Map<Boolean, ParsedTime> pCache = buildCache(constructor, time);
		ParsedTime ret = pCache.get(priority);
		if (ret == null) pCache.put(priority,  ret = constructor.apply(time, priority));
		return ret;
	}
	public static void clearCache() {
		cache.clear();
		buildCache(Unset::new, Unset.NORMAL.timeInTicks).put(Unset.NORMAL.priority, Unset.NORMAL);
	}
	private static Map<Boolean, ParsedTime> buildCache(BiFunction<Integer, Boolean, ParsedTime> constructor, int time) {
		return cache.computeIfAbsent(constructor, k -> new HashMap<>()).computeIfAbsent(time, k -> new HashMap<>());
	}

}
