package com.unascribed.fabrication.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

//searches nbt for an item identifier returns CONTINUE on fail, HALT on success
public class ItemNbtScanner implements NbtScanner {
	public static final ItemNbtScanner INSTANCE = new ItemNbtScanner();
	public static final TagKey<Item> EXCEPTIONS = TagKey.of(RegistryKeys.ITEM, new Identifier("fabrication", "exclude_from_item_inventory_check"));
	public static boolean hasItemInvNBT(ItemStack stack) {
		if (stack.isIn(EXCEPTIONS)) return false;
		NbtCompound tag = stack.getNbt();
		return tag != null && tag.doAccept(INSTANCE.reset()) == Result.HALT;
	}
	public Result ret = Result.CONTINUE;
	@Override
	public Result visitEnd() {
		return ret;
	}

	@Override
	public Result visitString(String value) {
		Identifier id = Identifier.tryParse(value);
		if (id != null && Registries.ITEM.containsId(id)) {
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
		if (type == NbtList.TYPE || type == NbtString.TYPE || type == NbtCompound.TYPE) return NestedResult.ENTER;
		return NestedResult.SKIP;
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

	public ItemNbtScanner reset() {
		ret = Result.CONTINUE;
		return this;
	}
}
