package com.unascribed.fabrication.logic;

import java.util.Locale;

public enum PlayerTag {
	INVISIBLE_TO_MOBS,
	NO_HUNGER,
	FIREPROOF,
	CAN_BREATHE_WATER,
	PERMANENT_DOLPHINS_GRACE,
	PERMANENT_CONDUIT_POWER,
	SCARES_CREEPERS,
	NO_PHANTOMS,
	;

	public String lowerName() {
		return name().toLowerCase(Locale.ROOT);
	}
}
