package com.unascribed.fabrication.interfaces;

public interface ToggleableFurnaceCart {
	int fabrication$tgfc$getPauseFuel();
	void fabrication$tgfc$setFuel(int fuel);
	static int get(Object o) {
		return o instanceof ToggleableFurnaceCart ? ((ToggleableFurnaceCart) o).fabrication$tgfc$getPauseFuel() : 0;
	}
}
