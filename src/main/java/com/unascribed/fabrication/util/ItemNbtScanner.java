package com.unascribed.fabrication.util;

import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

//searches nbt for an item identifier returns CONTINUE on fail, HALT on success
public class ItemNbtScanner implements NbtScanner {
	Result ret = Result.CONTINUE;
	@Override
	public Result visitEnd() {
		return ret;
	}

	@Override
	public Result visitString(String value) {
		Identifier id = Identifier.tryParse(value);
		if (id != null && Registry.ITEM.containsId(id)) {
			ret = Result.HALT;
		}
		return ret;
	}

	@Override
	public Result visitByte(byte value) {
		return Result.CONTINUE;
	}

	@Override
	public Result visitShort(short value) {
		return Result.CONTINUE;
	}

	@Override
	public Result visitInt(int value) {
		return Result.CONTINUE;
	}

	@Override
	public Result visitLong(long value) {
		return Result.CONTINUE;
	}

	@Override
	public Result visitFloat(float value) {
		return Result.CONTINUE;
	}

	@Override
	public Result visitDouble(double value) {
		return Result.CONTINUE;
	}

	@Override
	public Result visitByteArray(byte[] value) {
		return Result.CONTINUE;
	}

	@Override
	public Result visitIntArray(int[] value) {
		return Result.CONTINUE;
	}

	@Override
	public Result visitLongArray(long[] value) {
		return Result.CONTINUE;
	}

	@Override
	public Result visitListMeta(NbtType<?> entryType, int length) {
		return Result.CONTINUE;
	}

	@Override
	public NestedResult visitSubNbtType(NbtType<?> type) {
		return NestedResult.ENTER;
	}

	@Override
	public NestedResult startSubNbt(NbtType<?> type, String key) {
		return NestedResult.ENTER;
	}

	@Override
	public NestedResult startListItem(NbtType<?> type, int index) {
		return NestedResult.ENTER;
	}

	@Override
	public Result endNested() {
		return ret;
	}

	@Override
	public Result start(NbtType<?> rootType) {
		return Result.CONTINUE;
	}
}
