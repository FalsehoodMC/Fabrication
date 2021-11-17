package com.unascribed.fabrication.interfaces;

import net.minecraft.nbt.NbtList;

public interface SetCanHitList {

	void fabrication$setCanHitLists(NbtList list, NbtList list2);
	NbtList fabrication$getCanHitList();
	NbtList fabrication$getCanHitList2();
	
}
