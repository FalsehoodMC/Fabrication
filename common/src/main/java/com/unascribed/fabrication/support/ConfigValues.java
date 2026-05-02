package com.unascribed.fabrication.support;

import java.util.Locale;

public class ConfigValues {

	public enum Category {
		GREEN {
			@Override
			public int getColor() {
				return 0xFF8BC34A;
			}

			@Override
			public String displayDesc() {
				return "Enables nothing by default. Build your own.";
			}
		},/*
		BLONDE {
			@Override
			public int getColor() {
				return 0xFFFFCC80;
			}
		},
		LIGHT {
			@Override
			public int getColor() {
				return 0xFFA1887F;
			}
		},
		MEDIUM {
			@Override
			public int getColor() {
				return 0xFF6D4C41;
			}
		},*/
		DARK {
			@Override
			public int getColor() {
				return 0xFF4E342E;
			}

			@Override
			public String displayDesc() {
				return "Enable all but extra.";
			}
		},/*
		VIENNA {
			@Override
			public int getColor() {
				return 0xFF2B1B18;
			}
		},
		BURNT {
			@Override
			public int getColor() {
				return 0xFF12181B;
			}
		},*/
		ASH {
			@Override
			public int getColor() {
				return 0xFFAAAAAA;
			}

			@Override
			public String displayDesc() {
				return "Really enable everything.";
			}
		};
		public abstract int getColor();
		public static final String[] vals = stringValues();
		public static boolean isCategory(String str) {
			try {
				return parse(str) != null;
			} catch (IllegalArgumentException e) {
				return false;
			}
		}
		public static String[] stringValues() {
			Category[] values = values();
			String[] out = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				out[i] = values[i].name().toLowerCase(Locale.ROOT);
			}
			return out;
		}
		public static Category parse(String s) {
			switch (s = s.toUpperCase(Locale.ROOT)) {
				case "TRUE" : return DARK;
				case "BANNED": case "UNSET" : case "FALSE" : return GREEN;
			}
			return valueOf(s);
		}

		public String displayName() {
			return name().charAt(0) + (name().substring(1).toLowerCase(Locale.ROOT));
		}
		public abstract String displayDesc();
	}

	public enum Feature {
		UNSET,
		TRUE,
		FALSE,
		BANNED;

		public boolean resolve(boolean def) {
			if (this == TRUE) return true;
			if (this == FALSE || this == BANNED) return false;
			return def;
		}
		public static final String[] vals_true_false = new String[]{"true", "false"};
		public static final String[] vals_unset_true_false = new String[]{"unset", "true", "false"};
		public static final String[] vals_unset_true_false_banned = new String[]{"unset", "true", "false", "banned"};

		public ResolvedFeature resolveSemantically(boolean def) {
			if (this == BANNED) return ResolvedFeature.BANNED;
			if (this == TRUE) return ResolvedFeature.TRUE;
			if (this == FALSE) return ResolvedFeature.FALSE;
			return def ? ResolvedFeature.DEFAULT_TRUE : ResolvedFeature.DEFAULT_FALSE;
		}

		public static Feature parse(String s) {
			return valueOf(s.toUpperCase(Locale.ROOT));
		}
	}

	public enum ResolvedFeature {
		TRUE(true, Feature.TRUE),
		FALSE(false, Feature.FALSE),
		DEFAULT_TRUE(true, Feature.UNSET),
		DEFAULT_FALSE(false, Feature.UNSET),
		BANNED(false, Feature.BANNED),
		;

		public final boolean value;
		public final Feature feature;

		ResolvedFeature(boolean value, Feature feature) {
			this.value = value;
			this.feature = feature;
		}
	}
}
