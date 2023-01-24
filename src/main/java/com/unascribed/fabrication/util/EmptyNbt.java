package com.unascribed.fabrication.util;

import net.minecraft.nbt.NbtCompound;

public class EmptyNbt {
	//This exists because forgery is jank
	public static final NbtCompound TAG = new NbtCompound();
	public static NbtCompound getCopy() {
		return TAG.copy();
	}
}
