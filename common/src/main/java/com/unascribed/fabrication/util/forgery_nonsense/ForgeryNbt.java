package com.unascribed.fabrication.util.forgery_nonsense;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class ForgeryNbt {
	//This exists because forgery is jank
	public static NbtCompound getCompound() {
		return new NbtCompound();
	}
	public static NbtList getList() {
		return new NbtList();
	}
}
